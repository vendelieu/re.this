package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.ClusterSlavesCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.clusterSlaves(nodeId: String): List<String> {
    val request = if (cfg.withSlots) {
        ClusterSlavesCommandCodec.encodeWithSlot(charset = cfg.charset, nodeId = nodeId)
    } else {
        ClusterSlavesCommandCodec.encode(charset = cfg.charset, nodeId = nodeId)
    }
    return ClusterSlavesCommandCodec.decode(topology.handle(request), cfg.charset)
}
