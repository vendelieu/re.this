package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.types.interfaces.ReadFromStrategy

sealed class ReadFrom : ReadFromStrategy {
    object Master : ReadFromStrategy {
        override fun pick(request: CommandRequest, snapshot: Snapshot) = when (snapshot) {
            is SentinelSnapshot -> snapshot.providers[snapshot.masterIdx]

            is ClusterSnapshot -> request.computedSlot?.let {
                snapshot.providers[snapshot.slotOwner[it]]
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

    object Replica : ReadFromStrategy {
        override fun pick(request: CommandRequest, snapshot: Snapshot) = when (snapshot) {
            is SentinelSnapshot -> snapshot.providers.withIndex().first {
                it.index != snapshot.masterIdx
            }.value

            is ClusterSnapshot -> request.computedSlot?.let {
                val repIndices = snapshot.replicaIndices[it]
                val idx = repIndices.random()
                snapshot.providers[idx]
            } ?: snapshot.providers.first()
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

                is ClusterSnapshot -> request.computedSlot?.let { slot ->
                    val replicas = snapshot.replicaIndices[slot]
                    if (replicas.isNotEmpty()) {
                        snapshot.providers[replicas.random()]
                    } else {
                        snapshot.providers[snapshot.slotOwner[slot]]
                    }
                } ?: snapshot.providers.first()
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
                    // Build a set of all master indices (one per slot)
                    val masters = snapshot.slotOwner.toSet()
                    val replicaIndices = snapshot.providers.indices.filter { it !in masters }
                    val idx = if (replicaIndices.isNotEmpty())
                        replicaIndices.random()
                    else
                    // fallback: pick some master if no replica
                        snapshot.slotOwner[request.computedSlot ?: 0]
                    return snapshot.providers[idx]
                }
            }
        }
    }

    object LowestLatency : ReadFromStrategy {
        override fun pick(request: CommandRequest, snapshot: Snapshot) =
            snapshot.latencies
                .entries
                .minByOrNull { it.value }
                ?.key?.let {
                    snapshot.providers[it]
                } ?: snapshot.providers.first()
    }
}
