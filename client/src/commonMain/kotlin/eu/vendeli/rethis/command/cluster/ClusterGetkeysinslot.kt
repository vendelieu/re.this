package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.ClusterGetKeysInSlotCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.clusterGetKeysInSlot(slot: Long, count: Long): List<String> {
    val request = if(cfg.withSlots) {
        ClusterGetKeysInSlotCommandCodec.encodeWithSlot(charset = cfg.charset, slot = slot, count = count)
    } else {
        ClusterGetKeysInSlotCommandCodec.encode(charset = cfg.charset, slot = slot, count = count)
    }
    return ClusterGetKeysInSlotCommandCodec.decode(topology.handle(request), cfg.charset)
}
