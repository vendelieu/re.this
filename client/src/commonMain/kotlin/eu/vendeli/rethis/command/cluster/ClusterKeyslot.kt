package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.ClusterKeySlotCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.clusterKeySlot(key: String): Long {
    val request = if (cfg.withSlots) {
        ClusterKeySlotCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        ClusterKeySlotCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return ClusterKeySlotCommandCodec.decode(topology.handle(request), cfg.charset)
}
