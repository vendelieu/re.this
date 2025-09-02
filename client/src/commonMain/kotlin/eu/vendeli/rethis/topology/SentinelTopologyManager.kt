package eu.vendeli.rethis.topology

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.utils.unwrap
import eu.vendeli.rethis.codecs.connection.PingCommandCodec
import eu.vendeli.rethis.codecs.sentinel.SentinelGetMasterAddrCommandCodec
import eu.vendeli.rethis.codecs.sentinel.SentinelReplicasCommandCodec
import eu.vendeli.rethis.configuration.SentinelConfiguration
import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.providers.withConnection
import eu.vendeli.rethis.types.common.*
import eu.vendeli.rethis.types.interfaces.SubscriptionHandler
import eu.vendeli.rethis.utils.ClusterEventNames
import eu.vendeli.rethis.utils.panic
import eu.vendeli.rethis.utils.registerSubscription
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration
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
    private val scope = CoroutineScope(cfg.dispatcher + Job(client.rootJob))

    init {
        initialize()
    }

    private fun initialize() = scope.launch {
        safeRefresh()
        if (cfg.periodicRefresh) while (isActive) {
            delay(cfg.periodicRefreshInterval)
            safeRefresh()
        }
        subscribeToSentinels()
    }

    private suspend fun subscribeToSentinels() {
        val handler = SubscriptionHandler { _, _ -> reactiveRefresh() }
        snapshot.load()?.providers?.forEach {
            client.registerSubscription(
                ClusterEventNames.SWITCH_MASTER.name,
                Subscription(SubscriptionType.PLAIN, handler),
                it,
            )
        }
    }

    private fun reactiveRefresh() = scope.launch { if (!refreshMutex.isLocked) safeRefresh() }

    private suspend fun safeRefresh() = refreshMutex.withLock {
        // 1) discover master + replicas
        val (masterNode, replicaNodes) = fetchSentinelTopology()

        // 2) reconcile providers
        val oldSnap = snapshot.load()
        val oldProviders = oldSnap?.providers.orEmpty()
        val idxMap = oldProviders.mapIndexed { i, p -> p.node to i }.toMap()

        val allAddrs = listOf(masterNode) + replicaNodes
        val newProviders = allAddrs.mapIndexed { idx, addr ->
            idxMap[addr]?.let { oldProviders[it] }
                ?: client.connectionProviderFactory.create(addr)
        }.toTypedArray()

        // schedule closing old unused
        oldProviders.filter {
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
            val latencies = mutableMapOf<Int, Duration>()
            newProviders.forEachIndexed { idx, prov ->
                val rtt = measureLatency(prov)
                latencies[idx] = rtt
            }
            snap.latencies.putAll(latencies)
        }

        // 4) publish new snapshot
        snapshot.store(snap)
    }

    private suspend fun fetchSentinelTopology(): Pair<Address, List<Address>> {
        // try each sentinel until one succeeds
        sentinelNodes.forEach { sentinel ->
            val conn = client.connectionFactory.createConnOrNull(sentinel.socket) ?: return@forEach
            try {
                val master = conn.getMasterAddress(masterName)
                val replicas = conn.getSlaveAddresses(masterName)
                return master to replicas
            } catch (e: Throwable) {
                logger.debug("Failed to fetch Sentinel topology for '$masterName' from $sentinel", e)
            } finally {
                client.connectionFactory.dispose(conn)
            }
        }
        error("Unable to fetch Sentinel topology for '$masterName'")
    }

    override suspend fun route(request: CommandRequest) = snapshot.load()?.let {
        if (request.operation == RedisOperation.WRITE) return it.providers[it.masterIdx]
        cfg.readFromStrategy.pick(request, it)
    } ?: panic("Sentinel topology not initialized")

    override fun close() {
        scope.cancel()
        snapshot.load()?.providers?.forEach { it.close() }
    }

    private suspend fun measureLatency(provider: ConnectionProvider) = measureTime {
        provider.withConnection { it.doRequest(PingCommandCodec.encode(Charsets.UTF_8, null).buffer) }
    }

    private suspend fun RConnection.getMasterAddress(masterName: String): Address {
        val response = doRequest(SentinelGetMasterAddrCommandCodec.encode(Charsets.UTF_8, masterName).buffer)
        val result = SentinelGetMasterAddrCommandCodec.decode(response, Charsets.UTF_8)

        require(result.size == 2)
        return Address(result.first(), result.last().toInt())
    }

    private suspend fun RConnection.getSlaveAddresses(masterName: String): List<Address> {
        // send: SENTINEL slaves <masterName>
        val response = doRequest(SentinelReplicasCommandCodec.encode(Charsets.UTF_8, masterName).buffer)
        return SentinelReplicasCommandCodec.decode(response, Charsets.UTF_8).mapNotNull {
            // format explained here: https://redis.io/docs/latest/commands/cluster-nodes/
            val parts = it.unwrap<String>()!!.split(' ')
            if (!parts[4].contains("slave")) return@mapNotNull null
            val address = parts[1].substringBefore('@').split(':')
            Address(address[0], address[1].toInt())
        }
    }
}
