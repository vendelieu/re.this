package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.shared.types.ClusterException
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.ReThisException
import eu.vendeli.rethis.types.interfaces.ReadFromStrategy
import kotlin.time.Duration

/** Provider index of the master owning [slot]; fails fast when the slot is outside the known topology. */
internal fun ClusterSnapshot.masterIndexOf(slot: Int): Int =
    slotOwner.getOrNull(slot)?.takeIf { it >= 0 } ?: throw ClusterException("No master for slot $slot")

/**
 * Provider indices that may serve a read of [slot]: the replicas of the shard owning it, or its master when the
 * shard has none. Fails fast when the slot is outside the known topology.
 */
internal fun ClusterSnapshot.replicasOrMasterOf(slot: Int): IntArray {
    val masterIdx = masterIndexOf(slot)
    return replicaIndices[masterIdx].takeIf { it.isNotEmpty() } ?: intArrayOf(masterIdx)
}

sealed class ReadFrom : ReadFromStrategy {
    object Master : ReadFromStrategy {
        override fun pick(request: CommandRequest, snapshot: Snapshot) = when (snapshot) {
            is SentinelSnapshot -> snapshot.providers[snapshot.masterIdx]

            is ClusterSnapshot -> request.computedSlot?.let {
                snapshot.providers[snapshot.masterIndexOf(it)]
            } ?: snapshot.providers.first()
        }
    }

    object MasterPreferred : ReadFromStrategy {
        override fun pick(request: CommandRequest, snapshot: Snapshot): ConnectionProvider {
            val master = Master.pick(request, snapshot)
            return runCatching {
                if (master.hasSpareConnection()) master else Replica.pick(request, snapshot)
            }.getOrElse { master }
        }
    }

    /**
     * Reads only from replicas. Strict: when no replica is available a [ReThisException] is thrown
     * instead of falling back to the master (use [ReplicaPreferred] for that).
     */
    object Replica : ReadFromStrategy {
        override fun pick(request: CommandRequest, snapshot: Snapshot) = when (snapshot) {
            is SentinelSnapshot -> {
                snapshot.providers
                    .withIndex()
                    .firstOrNull {
                        it.index != snapshot.masterIdx
                    }?.value ?: throw ReThisException("No replica available for ReadFrom.Replica")
            }

            is ClusterSnapshot -> {
                request.computedSlot?.let {
                    val repIndices = snapshot.replicaIndices[snapshot.masterIndexOf(it)]
                    if (repIndices.isEmpty()) throw ReThisException("No replica available for slot $it")
                    val idx = repIndices.random()
                    snapshot.providers[idx]
                } ?: snapshot.providers.first()
            }
        }
    }

    object ReplicaPreferred : ReadFromStrategy {
        override fun pick(request: CommandRequest, snapshot: Snapshot): ConnectionProvider =
            when (snapshot) {
                is SentinelSnapshot -> {
                    // Try a random non-master; if none or empty, use master
                    val master = snapshot.masterIdx
                    val choices = snapshot.providers.indices.filter { it != master }
                    val idx = if (choices.isNotEmpty()) choices.random() else master
                    snapshot.providers[idx]
                }

                is ClusterSnapshot -> {
                    request.computedSlot?.let { slot ->
                        val masterIdx = snapshot.masterIndexOf(slot)
                        val replicas = snapshot.replicaIndices[masterIdx]
                        if (replicas.isNotEmpty()) {
                            snapshot.providers[replicas.random()]
                        } else {
                            snapshot.providers[masterIdx]
                        }
                    } ?: snapshot.providers.first()
                }
            }
    }

    object Any : ReadFromStrategy {
        override fun pick(request: CommandRequest, snapshot: Snapshot) = snapshot.providers.random()
    }

    object AnyReplica : ReadFromStrategy {
        override fun pick(request: CommandRequest, snapshot: Snapshot): ConnectionProvider {
            when (snapshot) {
                is SentinelSnapshot -> {
                    val master = snapshot.masterIdx
                    val replicas = snapshot.providers.indices.filter { it != master }
                    val idx = if (replicas.isNotEmpty()) replicas.random() else master
                    return snapshot.providers[idx]
                }

                is ClusterSnapshot -> {
                    // a keyed read stays in the shard of its slot; a keyless one may go to any replica
                    request.computedSlot?.let {
                        return snapshot.providers[snapshot.replicasOrMasterOf(it).random()]
                    }
                    val masters = snapshot.slotOwner.filter { it >= 0 }.toSet()
                    val replicaIndices = snapshot.providers.indices.filter { it !in masters }
                    val idx = if (replicaIndices.isNotEmpty())
                        replicaIndices.random()
                    else
                        // fallback: pick some master if no replica
                        snapshot.masterIndexOf(0)
                    return snapshot.providers[idx]
                }
            }
        }
    }

    /**
     * Picks the node with the lowest measured latency. In a cluster the choice is limited to the replicas of the
     * shard owning the slot (its master when it has none); nodes without a measurement rank last.
     */
    object LowestLatency : ReadFromStrategy {
        override fun pick(request: CommandRequest, snapshot: Snapshot): ConnectionProvider {
            val slot = request.computedSlot
            if (snapshot is ClusterSnapshot && slot != null) {
                val candidates = snapshot.replicasOrMasterOf(slot)
                val idx = candidates.minByOrNull { snapshot.latencies[it] ?: Duration.INFINITE } ?: candidates.first()
                return snapshot.providers[idx]
            }

            return snapshot.latencies
                .entries
                .minByOrNull { it.value }
                ?.key
                ?.let {
                    snapshot.providers[it]
                } ?: snapshot.providers.first()
        }
    }
}
