package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.providers.ConnectionProvider
import kotlin.time.Duration

sealed class Snapshot {
    abstract val providers: Array<ConnectionProvider>
    val latencies: MutableMap<Int, Duration> = mutableMapOf() // index -> latency
}

class SentinelSnapshot(
    val masterIdx: Int,
    override val providers: Array<ConnectionProvider>,
) : Snapshot()

class ClusterSnapshot(
    val slotOwner: IntArray,
    val replicaIndices: Array<IntArray>,
    override val providers: Array<ConnectionProvider>,
) : Snapshot()
