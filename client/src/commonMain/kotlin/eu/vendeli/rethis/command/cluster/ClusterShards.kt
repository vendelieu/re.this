package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.response.cluster.Shard
import eu.vendeli.rethis.codecs.cluster.ClusterShardsCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.collections.List

public suspend fun ReThis.clusterShards(): List<Shard> {
    val request = if(cfg.withSlots) {
        ClusterShardsCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        ClusterShardsCommandCodec.encode(charset = cfg.charset, )
    }
    return ClusterShardsCommandCodec.decode(topology.handle(request), cfg.charset)
}
