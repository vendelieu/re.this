package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.ClusterReplicasCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.clusterReplicas(nodeId: String): List<String> {
    val request = if(cfg.withSlots) {
        ClusterReplicasCommandCodec.encodeWithSlot(charset = cfg.charset, nodeId = nodeId)
    } else {
        ClusterReplicasCommandCodec.encode(charset = cfg.charset, nodeId = nodeId)
    }
    return ClusterReplicasCommandCodec.decode(topology.handle(request), cfg.charset)
}
