package eu.vendeli.rethis.topology

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.response.cluster.ClusterNode
import eu.vendeli.rethis.api.spec.common.types.*
import eu.vendeli.rethis.codecs.cluster.AskingCommandCodec
import eu.vendeli.rethis.codecs.cluster.ClusterSlotsCommandCodec
import eu.vendeli.rethis.configuration.ClusterConfiguration
import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.providers.withConnection
import eu.vendeli.rethis.types.common.Address
import eu.vendeli.rethis.types.common.ClusterSnapshot
import eu.vendeli.rethis.utils.toAddress
import eu.vendeli.rethis.utils.toHostAndPort
import eu.vendeli.rethis.utils.withRetry
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.Buffer
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class ClusterTopologyManager(
    private val initialNodes: List<Address>,
    private val client: ReThis,
    private val cfg: ClusterConfiguration,
) : TopologyManager {
    private val snapshotRef: AtomicReference<ClusterSnapshot?> = AtomicReference(null)
    private val refreshMutex = Mutex()
    private val scope = CoroutineScope(cfg.dispatcher + Job(client.rootJob))
    override val retryCfg = cfg.retry

    init {
        initialize()
    }

    private fun initialize() = scope.launch {
        fullRefresh()

        if (cfg.periodicRefresh) {
            while (isActive) {
                delay(cfg.periodicRefreshInterval)
                fullRefresh()
            }
        }
    }

    private suspend fun fullRefresh() = refreshMutex.withLock {
        if (!scope.isActive) throw ClusterException("Topology is closed")
        // 1) Fetch CLUSTER SLOTS
        val entries = fetchClusterSlots()

        // 2) Build new providers array
        val oldSnap = snapshotRef.load()
        val oldProviders = oldSnap?.providers ?: emptyArray()
        val oldNodeToIdx = oldProviders.mapIndexed { i, p -> p.node to i }.toMap()
        val allNodes = entries.flatMap { listOf(it.master) + it.replicas }.distinct()
        val newProviders = allNodes.mapIndexed { _, node ->
            oldNodeToIdx[node.toAddress()]?.let {
                oldProviders[it]
            } ?: client.connectionProviderFactory.create(node.toAddress())
        }.toTypedArray()

        // cool-down old orphans
        oldProviders.filter { c ->
            c.node.toHostAndPort()?.let { it !in allNodes } ?: false
        }.forEach {
            scope.launch { it.close() }
        }

        // 3) Dense slotOwner
        val slotOwner = IntArray(16_384)
        val nodeIdxMap = allNodes.mapIndexed { i, n -> n to i }.toMap()
        entries.forEach { (master, ranges, _) ->
            val mIdx = nodeIdxMap[master]!!
            ranges.forEach {
                for (s in it.start..it.end) slotOwner[s.toInt()] = mIdx
            }
        }

        // 4) Precompute replicas
        val replicaIndices = Array(newProviders.size) { IntArray(0) }
        entries.forEach { (master, _, replicas) ->
            val mIdx = nodeIdxMap[master]!!
            replicaIndices[mIdx] = replicas.map { nodeIdxMap[it]!! }.toIntArray()
        }

        // 5) Publish
        snapshotRef.store(
            ClusterSnapshot(
                slotOwner = slotOwner,
                providers = newProviders,
                replicaIndices = replicaIndices,
            ),
        )
    }

    override suspend fun handleFailure(request: CommandRequest, exception: Throwable): Buffer = when (exception) {
        is RedirectAskException -> route(request).withConnection { conn ->
            conn.doBatchRequest(listOf(AskingCommandCodec.encode(cfg.charset).buffer, request.buffer))
        }

        is RedirectMovedException -> {
            snapshotRef.load()?.also { snap ->
                val target = Address(exception.host, exception.port)
                val idx = snap.providers.indexOfFirst { it.node == target }.takeIf { it >= 0 } ?: run {
                    // on-demand add
                    val newProvider = client.connectionProviderFactory.create(target)
                    val providers = snap.providers + newProvider
                    val slots = snap.slotOwner.apply { this[exception.slot] = providers.lastIndex }
                    val replicas = snap.replicaIndices + IntArray(0)
                    snapshotRef.store(ClusterSnapshot(slots, replicas, providers))
                    providers.lastIndex
                }
                snap.slotOwner[exception.slot] = idx
            }
            delay(cfg.movedBackoffPeriod)
            throw exception
        }

        else -> {
            delay(cfg.movedBackoffPeriod)
            throw exception
        }
    }

    override suspend fun route(request: CommandRequest): ConnectionProvider {
        val snap = snapshotRef.load() ?: throw ClusterException("Cluster not ready")
        val slot = request.computedSlot ?: return snap.providers.first()

        val masterIdx = snap.slotOwner.getOrNull(slot) ?: throw ClusterException("No master for slot $slot")
        if (request.operation == RedisOperation.WRITE) return snap.providers[masterIdx]

        return cfg.readFromStrategy.pick(request, snap)
    }

    override fun close() {
        scope.cancel()
        snapshotRef.load()?.providers?.forEach { it.close() }
    }

    private suspend fun fetchClusterSlots(): List<ClusterNode> {
        val initialNodes = ArrayDeque(initialNodes)
        return withRetry(cfg.retry) {
            val req = ClusterSlotsCommandCodec.encode(Charsets.UTF_8)
            val conn = client.connectionFactory.createConnOrNull(initialNodes.removeFirst().socket)
                ?: throw ClusterException("Can't create connection while fetching cluster slots")
            val response = try {
                conn.doRequest(req.buffer)
            } finally {
                client.connectionFactory.dispose(conn)
            }

            ClusterSlotsCommandCodec.decode(response, Charsets.UTF_8).nodes
        }
    }
}
