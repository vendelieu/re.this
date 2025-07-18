package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.ClusterNodesCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String

public suspend fun ReThis.clusterNodes(): String {
    val request = if(cfg.withSlots) {
        ClusterNodesCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        ClusterNodesCommandCodec.encode(charset = cfg.charset, )
    }
    return ClusterNodesCommandCodec.decode(topology.handle(request), cfg.charset)
}
