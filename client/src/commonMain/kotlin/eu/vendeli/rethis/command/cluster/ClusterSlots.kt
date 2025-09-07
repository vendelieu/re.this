package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.ClusterSlotsCommandCodec
import eu.vendeli.rethis.shared.response.cluster.Cluster
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.clusterSlots(): Cluster {
    val request = if(cfg.withSlots) {
        ClusterSlotsCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        ClusterSlotsCommandCodec.encode(charset = cfg.charset, )
    }
    return ClusterSlotsCommandCodec.decode(topology.handle(request), cfg.charset)
}
