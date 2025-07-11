package eu.vendeli.rethis.topology

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.codecs.connection.PingCommandCodec
import eu.vendeli.rethis.codecs.sentinel.SentinelGetMasterAddrCommandCodec
import eu.vendeli.rethis.codecs.sentinel.SentinelReplicasCommandCodec
import eu.vendeli.rethis.configuration.SentinelConfiguration
import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.providers.withConnection
import eu.vendeli.rethis.types.common.Address
import eu.vendeli.rethis.types.common.RConnection
import eu.vendeli.rethis.types.common.ReadFrom
import eu.vendeli.rethis.types.common.SentinelSnapshot
import eu.vendeli.rethis.utils.panic
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
    private val cfg: SentinelConfiguration,
) : TopologyManager {
    private val snapshot: AtomicReference<SentinelSnapshot?> = AtomicReference(null)
    private val refreshMutex = Mutex()
    private val scope = CoroutineScope(cfg.dispatcher + Job(client.rootJob))
    override val retryCfg = cfg.retry

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

    private suspend fun subscribeToSentinels() = sentinelNodes.forEach { sn ->
        scope.launch {
            // subscribe to switch-master
        }
    }

    private fun reactiveRefresh() = scope.launch { safeRefresh() }

    private suspend fun safeRefresh() = refreshMutex.withLock {
        // 1) discover master + replicas
        val (masterNode, replicaNodes) = fetchSentinelTopology()

        // 2) reconcile providers
        val oldSnap = snapshot.load()
        val oldProviders = oldSnap?.providers ?: emptyArray()
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
            } catch (_: Throwable) {
                // todo log
                // try next
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

    override suspend fun handleFailure(exception: Throwable) {}

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
        // response: List<String> [host, port]
        require(result.size == 2)
        return Address(result.first(), result.last().toInt())
    }

    private suspend fun RConnection.getSlaveAddresses(masterName: String): List<Address> {
        // send: SENTINEL slaves <masterName>
        val response = doRequest(SentinelReplicasCommandCodec.encode(Charsets.UTF_8, masterName).buffer)
        val result = SentinelReplicasCommandCodec.decode(response, Charsets.UTF_8)
        TODO()
    }
}
