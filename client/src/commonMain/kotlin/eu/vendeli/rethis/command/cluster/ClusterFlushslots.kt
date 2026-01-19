package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.ClusterFlushSlotsCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.clusterFlushSlots(): Boolean {
    val request = if (cfg.withSlots) {
        ClusterFlushSlotsCommandCodec.encodeWithSlot(charset = cfg.charset)
    } else {
        ClusterFlushSlotsCommandCodec.encode(charset = cfg.charset)
    }
    return ClusterFlushSlotsCommandCodec.decode(topology.handle(request), cfg.charset)
}
