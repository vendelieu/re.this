package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.ClusterCountFailureReportsCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.clusterCountFailureReports(nodeId: String): Long {
    val request = if (cfg.withSlots) {
        ClusterCountFailureReportsCommandCodec.encodeWithSlot(charset = cfg.charset, nodeId = nodeId)
    } else {
        ClusterCountFailureReportsCommandCodec.encode(charset = cfg.charset, nodeId = nodeId)
    }
    return ClusterCountFailureReportsCommandCodec.decode(topology.handle(request), cfg.charset)
}
