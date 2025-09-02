package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.ClusterReplicateCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.clusterReplicate(nodeId: String): Boolean {
    val request = if(cfg.withSlots) {
        ClusterReplicateCommandCodec.encodeWithSlot(charset = cfg.charset, nodeId = nodeId)
    } else {
        ClusterReplicateCommandCodec.encode(charset = cfg.charset, nodeId = nodeId)
    }
    return ClusterReplicateCommandCodec.decode(topology.handle(request), cfg.charset)
}
