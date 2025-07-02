package eu.vendeli.rethis.api.spec.common.response.cluster

import eu.vendeli.rethis.api.spec.common.request.cluster.SlotRange

data class Shard(
    val slots: List<SlotRange>,
    val nodes: List<ShardNode>
)
