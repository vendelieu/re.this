package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.ClusterForgetCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.String

public suspend fun ReThis.clusterForget(nodeId: String): Boolean {
    val request = if(cfg.withSlots) {
        ClusterForgetCommandCodec.encodeWithSlot(charset = cfg.charset, nodeId = nodeId)
    } else {
        ClusterForgetCommandCodec.encode(charset = cfg.charset, nodeId = nodeId)
    }
    return ClusterForgetCommandCodec.decode(topology.handle(request), cfg.charset)
}
