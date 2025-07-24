package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.ClusterMyIdCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.clusterMyId(): String {
    val request = if(cfg.withSlots) {
        ClusterMyIdCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        ClusterMyIdCommandCodec.encode(charset = cfg.charset, )
    }
    return ClusterMyIdCommandCodec.decode(topology.handle(request), cfg.charset)
}
