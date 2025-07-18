package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.ClusterCountKeysInSlotCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long

public suspend fun ReThis.clusterCountKeysInSlot(slot: Long): Long {
    val request = if(cfg.withSlots) {
        ClusterCountKeysInSlotCommandCodec.encodeWithSlot(charset = cfg.charset, slot = slot)
    } else {
        ClusterCountKeysInSlotCommandCodec.encode(charset = cfg.charset, slot = slot)
    }
    return ClusterCountKeysInSlotCommandCodec.decode(topology.handle(request), cfg.charset)
}
