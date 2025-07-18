package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.ClusterDelSlotsCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.Long

public suspend fun ReThis.clusterDelSlots(vararg slot: Long): Boolean {
    val request = if(cfg.withSlots) {
        ClusterDelSlotsCommandCodec.encodeWithSlot(charset = cfg.charset, slot = slot)
    } else {
        ClusterDelSlotsCommandCodec.encode(charset = cfg.charset, slot = slot)
    }
    return ClusterDelSlotsCommandCodec.decode(topology.handle(request), cfg.charset)
}
