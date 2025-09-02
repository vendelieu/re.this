package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.ClusterBumpEpochCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.clusterBumpEpoch(): Boolean {
    val request = if(cfg.withSlots) {
        ClusterBumpEpochCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        ClusterBumpEpochCommandCodec.encode(charset = cfg.charset, )
    }
    return ClusterBumpEpochCommandCodec.decode(topology.handle(request), cfg.charset)
}
