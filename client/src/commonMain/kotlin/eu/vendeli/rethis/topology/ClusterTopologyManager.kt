package eu.vendeli.rethis.topology

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.AskingCommandCodec
import eu.vendeli.rethis.codecs.cluster.ClusterSlotsCommandCodec
import eu.vendeli.rethis.configuration.ClusterConfiguration
import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.providers.withConnection
import eu.vendeli.rethis.shared.response.cluster.ClusterNode
import eu.vendeli.rethis.shared.response.common.HostAndPort
import eu.vendeli.rethis.shared.types.*
import eu.vendeli.rethis.types.common.Address
import eu.vendeli.rethis.types.common.ClusterSnapshot
import eu.vendeli.rethis.types.common.masterIndexOf
import eu.vendeli.rethis.types.interfaces.ExperimentalReThisMetricsApi
import eu.vendeli.rethis.types.interfaces.RedirectKind
import eu.vendeli.rethis.utils.isTransientFailure
import eu.vendeli.rethis.utils.toAddress
import eu.vendeli.rethis.utils.toHostAndPort
import eu.vendeli.rethis.utils.withRetry
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration

@OptIn(ExperimentalAtomicApi::class, ExperimentalReThisMetricsApi::class)
class ClusterTopologyManager(
    private val initialNodes: List<Address>,
    private val client: ReThis,
    override val cfg: ClusterConfiguration,
) : TopologyManager {
    private val logger = cfg.loggerFactory.get("eu.vendeli.rethis.topology.ClusterTopologyManager")
    private val snapshotRef: AtomicReference<ClusterSnapshot?> = AtomicReference(null)
    private val refreshMutex = Mutex()
    private val onDemandRefresh = OnDemandRefresh(refreshMutex)
    private val scope = CoroutineScope(cfg.generalDispatcher + Job(client.rootJob))

    init {
        logger.info("Connecting to $initialNodes")
        initialize()
    }

    internal fun currentSnapshot(): ClusterSnapshot? = snapshotRef.load()

    private fun initialize() = scope.launch {
        runRefreshSafely(logger) { fullRefresh() }

        if (cfg.periodicRefresh) {
            while (isActive) {
                delay(cfg.periodicRefreshInterval)
                runRefreshSafely(logger) { fullRefresh() }
            }
        }
    }

    private suspend fun fullRefresh() = refreshMutex.withLock { refreshLocked() }

    private suspend fun refreshLocked() {
        val obs = client.cfg.metricsRecorder?.topologyRefreshStarted()
        var success = false
        try {
            if (!scope.isActive) throw ClusterException("Topology is closed")
            // 1) Fetch CLUSTER SLOTS
            val entries = fetchClusterSlots()

            publishSnapshot(entries)
            success = true
        } finally {
            obs?.completed(success)
        }
    }

    /**
     * Publishes the snapshot for [entries] with compare-and-set, rebuilding it when a concurrent MOVED update changed
     * the published one in the meantime so that update (and its provider) is not lost. Providers created for a lost
     * attempt are closed; providers no longer in the topology are closed once the new snapshot is published.
     */
    private fun publishSnapshot(entries: List<ClusterNode>) {
        while (true) {
            val oldSnap = snapshotRef.load()
            val newSnap = buildSnapshot(entries, oldSnap)
            val oldProviders = oldSnap?.providers.orEmpty()
            if (snapshotRef.compareAndSet(oldSnap, newSnap)) {
                val allNodes = entries.flatMap { listOf(it.master) + it.replicas }.distinct()
                oldProviders
                    .filter { c -> c.node.toHostAndPort()?.let { it !in allNodes } ?: false }
                    .forEach { scope.launch { it.close() } }
                return
            }
            newSnap.providers.filter { created -> oldProviders.none { it === created } }.forEach { it.close() }
        }
    }

    private fun buildSnapshot(entries: List<ClusterNode>, oldSnap: ClusterSnapshot?): ClusterSnapshot {
        // 2) Build new providers array
        val oldProviders = oldSnap?.providers.orEmpty()
        val oldNodeToIdx = oldProviders.mapIndexed { i, p -> p.node to i }.toMap()
        val allNodes = entries.flatMap { listOf(it.master) + it.replicas }.distinct()
        val newProviders = allNodes
            .mapIndexed { _, node ->
                oldNodeToIdx[node.toAddress()]?.let {
                    oldProviders[it]
                } ?: client.connectionProviderFactory.create(node.toAddress())
            }.toTypedArray()

        // 3) Dense slotOwner
        val nodeIdxMap = allNodes.mapIndexed { i, n -> n to i }.toMap()
        val slotOwner = buildSlotOwner(entries, nodeIdxMap)

        // 4) Precompute replicas
        val replicaIndices = Array(newProviders.size) { IntArray(0) }
        entries.forEach { (master, _, replicas) ->
            val mIdx = nodeIdxMap[master]!!
            replicaIndices[mIdx] = replicas.map { nodeIdxMap[it]!! }.toIntArray()
        }

        return ClusterSnapshot(
            slotOwner = slotOwner,
            providers = newProviders,
            replicaIndices = replicaIndices,
        )
    }

    override suspend fun handleFailure(request: CommandRequest, exception: Throwable): Buffer {
        val rec = cfg.metricsRecorder
        return when (exception) {
            is RedirectAskException -> {
                rec?.onClusterRedirect(RedirectKind.ASK)
                val snap = snapshotRef.load() ?: throw ClusterNotReadyException()
                val reply = withAskTarget(snap, exception, client.connectionProviderFactory::create) { provider ->
                    // a rejected ASKING leaves the connection clean, so it is released and the failure raised afterwards
                    provider
                        .withConnection { conn ->
                            requestAfterAskingCatching(AskingCommandCodec.encode(cfg.charset).data, request.data) {
                                conn.doRequest(it, commandTimeout = client.cfg.commandTimeoutFor(request))
                            }
                        }.getOrThrow()
                }
                // the slot may have finished moving, so the target can answer with MOVED
                try {
                    reply.throwIfRetryableError()
                    reply
                } catch (moved: RedirectMovedException) {
                    handleFailure(request, moved)
                }
            }

            is RedirectMovedException -> {
                rec?.onClusterRedirect(RedirectKind.MOVED)
                applyMoved(exception)
                delay(cfg.movedBackoffPeriod)
                throw exception
            }

            else -> {
                failAfterBackoff(exception, cfg.movedBackoffPeriod)
            }
        }
    }

    /**
     * Points the redirected slot at its new owner by publishing a new snapshot; the published one is never mutated,
     * since [route] reads its arrays concurrently. A target missing from the topology is added as a provider.
     * A slot outside the slot range is ignored, as the server text is not trusted.
     */
    private fun applyMoved(exception: RedirectMovedException) {
        if (exception.slot !in 0 until CLUSTER_SLOT_COUNT) {
            logger.warn("Ignoring MOVED redirect for out-of-range slot ${exception.slot}")
            return
        }
        val target = Address(exception.host, exception.port)
        var created: ConnectionProvider? = null
        var createdUsed = false
        while (true) {
            val snap = snapshotRef.load() ?: break
            val known = snap.providers.indexOfFirst { it.node == target }
            val next = if (known >= 0) {
                snap.withSlotOwner(exception.slot, known, snap.providers, snap.replicaIndices)
            } else {
                val provider = created ?: client.connectionProviderFactory.create(target).also { created = it }
                snap.withSlotOwner(
                    exception.slot,
                    snap.providers.size,
                    snap.providers + provider,
                    snap.replicaIndices + IntArray(0),
                )
            }
            if (snapshotRef.compareAndSet(snap, next)) {
                createdUsed = known < 0
                break
            }
        }
        if (!createdUsed) created?.close()
    }

    override suspend fun route(request: CommandRequest): ConnectionProvider {
        val snap = onDemandRefresh.ensure(snapshotRef::load, ::refreshLocked)
        val slot = request.computedSlot ?: return snap.providers.first()

        val masterIdx = snap.masterIndexOf(slot)
        if (request.operation == RedisOperation.WRITE) return snap.providers[masterIdx]

        return cfg.readFromStrategy.pick(request, snap)
    }

    override fun close() {
        scope.cancel()
        snapshotRef.load()?.providers?.forEach { it.close() }
    }

    // slot discovery is read-only, so a seed that does not answer in time is replaced by the next one
    private suspend fun fetchClusterSlots(): List<ClusterNode> = withRetry(
        cfg,
        { it.isTransientFailure() || it is CommandTimeoutException },
    ) { attempt ->
        val req = ClusterSlotsCommandCodec.encode(cfg.charset)
        val conn = client.connectionFactory.createConnOrIoError(initialNodes.seedFor(attempt).socket)
        val response = try {
            conn.doRequest(req.data, commandTimeout = cfg.commandTimeout)
        } finally {
            client.connectionFactory.dispose(conn)
        }

        ClusterSlotsCommandCodec.decode(response, cfg.charset).nodes
    }
}

private fun ClusterSnapshot.withSlotOwner(
    slot: Int,
    ownerIdx: Int,
    providers: Array<ConnectionProvider>,
    replicaIndices: Array<IntArray>,
) = ClusterSnapshot(slotOwner.copyOf().also { it[slot] = ownerIdx }, replicaIndices, providers).also {
    it.latencies.putAll(latencies)
}

internal const val CLUSTER_SLOT_COUNT = 16_384
internal const val UNCOVERED_SLOT = -1

/** Maps every slot to the provider index of its master, or [UNCOVERED_SLOT] when no node reports it. */
internal fun buildSlotOwner(entries: List<ClusterNode>, nodeIdxMap: Map<HostAndPort, Int>): IntArray {
    val slotOwner = IntArray(CLUSTER_SLOT_COUNT) { UNCOVERED_SLOT }
    entries.forEach { (master, ranges, _) ->
        val mIdx = nodeIdxMap.getValue(master)
        ranges.forEach {
            for (s in it.start..it.end) slotOwner[s.toInt()] = mIdx
        }
    }
    return slotOwner
}

/** Whether the failure comes from the connection or unstable cluster state, where waiting before the next attempt helps. */
internal fun Throwable.shouldBackOff(): Boolean = when (this) {
    is IOException,
    is CommandTimeoutException,
    is RespUnexpectedEOF,
    is RedirectMovedException,
    is RedirectUnstableException,
    is DownUnboundSlotException,
    is DownReadOnlyStateException,
    -> true

    else -> false
}

internal suspend fun failAfterBackoff(exception: Throwable, period: Duration): Nothing {
    if (exception.shouldBackOff()) delay(period)
    throw exception
}
