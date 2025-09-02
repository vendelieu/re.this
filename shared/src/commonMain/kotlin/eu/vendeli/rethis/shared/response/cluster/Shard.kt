package eu.vendeli.rethis.shared.response.cluster

import eu.vendeli.rethis.shared.request.cluster.SlotRange

data class Shard(
    val slots: List<SlotRange>,
    val nodes: List<ShardNode>
)
