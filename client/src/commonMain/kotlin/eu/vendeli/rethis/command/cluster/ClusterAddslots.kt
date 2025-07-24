package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.ClusterAddSlotsCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.clusterAddSlots(vararg slot: Long): Boolean {
    val request = if(cfg.withSlots) {
        ClusterAddSlotsCommandCodec.encodeWithSlot(charset = cfg.charset, slot = slot)
    } else {
        ClusterAddSlotsCommandCodec.encode(charset = cfg.charset, slot = slot)
    }
    return ClusterAddSlotsCommandCodec.decode(topology.handle(request), cfg.charset)
}
