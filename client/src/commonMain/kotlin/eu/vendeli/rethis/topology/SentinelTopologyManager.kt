package eu.vendeli.rethis.topology

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.connection.PingCommandCodec
import eu.vendeli.rethis.codecs.sentinel.SentinelGetMasterAddrCommandCodec
import eu.vendeli.rethis.codecs.sentinel.SentinelReplicasCommandCodec
import eu.vendeli.rethis.configuration.SentinelConfiguration
import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.providers.withConnection
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.ReThisException
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.types.common.*
import eu.vendeli.rethis.types.interfaces.MessageEventHandler
import eu.vendeli.rethis.types.interfaces.PubSubHandler
import eu.vendeli.rethis.types.interfaces.toPubSubHandler
import eu.vendeli.rethis.utils.ClusterEventNames
import eu.vendeli.rethis.utils.registerSubscription
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.measureTime

@OptIn(ExperimentalAtomicApi::class)
class SentinelTopologyManager(
    private val masterName: String,
    private val sentinelNodes: List<Address>,
    private val client: ReThis,
    override val cfg: SentinelConfiguration,
) : TopologyManager {
    private val logger = cfg.loggerFactory.get("eu.vendeli.rethis.SentinelTopologyManager")
    private val snapshot: AtomicReference<SentinelSnapshot?> = AtomicReference(null)
    private val refreshMutex = Mutex()
    private val sentinelProviders = SentinelProviderRegistry()
    private val scope = CoroutineScope(cfg.generalDispatcher + Job(client.rootJob))

    init {
        logger.info("Connecting to $sentinelNodes with $masterName as master")
        initialize()
    }

    private fun initialize() {
        scope.launch {
            runRefreshSafely(logger) { safeRefresh() }
            if (cfg.periodicRefresh) while (isActive) {
                delay(cfg.periodicRefreshInterval)
                runRefreshSafely(logger) { safeRefresh() }
            }
        }
        scope.launch { subscribeToSentinels() }
    }

    private fun subscribeToSentinels() {
        val handler = MessageEventHandler { _, message ->
            // +switch-master payload: <master-name> <old-ip> <old-port> <new-ip> <new-port>
            if (message.substringBefore(' ') == masterName) reactiveRefresh(message.switchMasterTarget())
        }.toPubSubHandler(client)
        sentinelNodes.forEach { sentinel -> scope.launch { keepSubscribed(sentinel, handler) } }
    }

    /**
     * Keeps a `+switch-master` subscription open on [sentinel]: a sentinel that cannot be reached, and a subscription
     * that ends because its connection dropped, are retried with the [cfg] retry backoff (bounded by `retry.maxDelay`,
     * without an attempt limit) until the manager is closed. A subscription cancelled on purpose is not re-opened.
     */
    private suspend fun keepSubscribed(sentinel: Address, handler: PubSubHandler) {
        val provider = client.connectionProviderFactory.create(sentinel)
        if (!sentinelProviders.add(provider)) return
        val target = SubscribeTarget.Channel(ClusterEventNames.SWITCH_MASTER.literal)
        var backoff = cfg.retry.initialDelay
        while (currentCoroutineContext().isActive) {
            try {
                val worker = client.subscriptions.registerSubscription(target, handler, provider)
                backoff = cfg.retry.initialDelay
                worker.join()
                if (worker.isCancelled) return
            } catch (e: TimeoutCancellationException) {
                logger.warn("Timed out subscribing to ${ClusterEventNames.SWITCH_MASTER.literal} on $sentinel", e)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.warn("Failed to subscribe to ${ClusterEventNames.SWITCH_MASTER.literal} on $sentinel", e)
            }
            delay(backoff)
            backoff = (backoff * cfg.retry.factor).coerceAtMost(cfg.retry.maxDelay)
        }
    }

    /** Refreshes unless the snapshot already points at [announcedMaster], which every sentinel repeats per failover. */
    private fun reactiveRefresh(announcedMaster: Address?) = scope.launch {
        runRefreshSafely(logger) {
            refreshMutex.withLock {
                if (announcedMaster == null || announcedMaster != currentMaster()) refreshLocked()
            }
        }
    }

    private fun currentMaster(): Address? = snapshot.load()?.let { it.providers[it.masterIdx].node }

    private suspend fun safeRefresh() = refreshMutex.withLock { refreshLocked() }

    private suspend fun refreshLocked() {
        // 1) discover master + replicas
        val (masterNode, replicaNodes) = fetchSentinelTopology()

        // 2) reconcile providers
        val oldSnap = snapshot.load()
        val oldProviders = oldSnap?.providers.orEmpty()
        val idxMap = oldProviders.mapIndexed { i, p -> p.node to i }.toMap()

        val allAddrs = listOf(masterNode) + replicaNodes
        val newProviders = allAddrs
            .mapIndexed { idx, addr ->
                idxMap[addr]?.let { oldProviders[it] }
                    ?: client.connectionProviderFactory.create(addr)
            }.toTypedArray()

        // schedule closing old unused
        oldProviders
            .filter {
                it.node !in allAddrs
            }.forEach {
                scope.launch { it.close() }
            }

        val snap = SentinelSnapshot(
            masterIdx = 0,
            providers = newProviders,
        )

        // 3) measure latencies if needed
        if (cfg.readFromStrategy == ReadFrom.LowestLatency) {
            snap.latencies.putAll(measureLatencies(newProviders, logger, ::measureLatency))
        }

        // 4) publish new snapshot
        snapshot.store(snap)
    }

    internal suspend fun fetchSentinelTopology(): Pair<Address, List<Address>> {
        // try each sentinel until one succeeds
        var lastFailure: Throwable? = null
        sentinelNodes.forEach { sentinel ->
            val conn = client.connectionFactory.createConnOrNull(sentinel.socket)
            if (conn == null) {
                lastFailure = IOException("Can't create connection to $sentinel")
                return@forEach
            }
            try {
                val master = conn.getMasterAddress(masterName)
                val replicas = conn.getSlaveAddresses(masterName)
                return master to replicas
            } catch (e: Throwable) {
                logger.debug("Failed to fetch Sentinel topology for '$masterName' from $sentinel", e)
                lastFailure = e
            } finally {
                client.connectionFactory.dispose(conn)
            }
        }
        throw IllegalStateException("Unable to fetch Sentinel topology for '$masterName'", lastFailure)
    }

    override suspend fun route(request: CommandRequest) = snapshot.load()?.let {
        if (request.operation == RedisOperation.WRITE) return it.providers[it.masterIdx]
        cfg.readFromStrategy.pick(request, it)
    } ?: throw ReThisException("Sentinel topology not initialized")

    override fun close() {
        scope.cancel()
        snapshot.load()?.providers?.forEach { it.close() }
        sentinelProviders.closeAll()
    }

    private suspend fun measureLatency(provider: ConnectionProvider) = measureTime {
        provider.withConnection {
            it.doRequest(PingCommandCodec.encode(Charsets.UTF_8, null).data, commandTimeout = cfg.commandTimeout)
        }
    }

    private suspend fun RConnection.getMasterAddress(masterName: String): Address {
        val response = doRequest(
            SentinelGetMasterAddrCommandCodec.encode(Charsets.UTF_8, masterName).data,
            commandTimeout = cfg.commandTimeout,
        )
        val result = SentinelGetMasterAddrCommandCodec.decode(response, Charsets.UTF_8)
            ?: error("Sentinel does not know master '$masterName'")

        require(result.size == 2)
        return Address(result.first(), result.last().toInt())
    }

    private suspend fun RConnection.getSlaveAddresses(masterName: String): List<Address> {
        // send: SENTINEL REPLICAS <masterName>
        val response = doRequest(
            SentinelReplicasCommandCodec.encode(Charsets.UTF_8, masterName).data,
            commandTimeout = cfg.commandTimeout,
        )
        return parseReplicaAddresses(SentinelReplicasCommandCodec.decode(response, Charsets.UTF_8), logger)
    }
}

/** New master address of a `+switch-master` payload (`<name> <old-ip> <old-port> <new-ip> <new-port>`), if it parses. */
internal fun String.switchMasterTarget(): Address? {
    val parts = split(' ')
    val port = parts.getOrNull(4)?.toIntOrNull() ?: return null
    return Address(parts[3], port)
}
