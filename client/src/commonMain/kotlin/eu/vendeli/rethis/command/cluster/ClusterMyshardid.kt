package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.ClusterMyShardIdCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.clusterMyShardId(): String {
    val request = if(cfg.withSlots) {
        ClusterMyShardIdCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        ClusterMyShardIdCommandCodec.encode(charset = cfg.charset, )
    }
    return ClusterMyShardIdCommandCodec.decode(topology.handle(request), cfg.charset)
}
