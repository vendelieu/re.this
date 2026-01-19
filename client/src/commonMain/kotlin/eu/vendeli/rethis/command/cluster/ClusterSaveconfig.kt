package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.ClusterSaveConfigCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.clusterSaveConfig(): Boolean {
    val request = if (cfg.withSlots) {
        ClusterSaveConfigCommandCodec.encodeWithSlot(charset = cfg.charset)
    } else {
        ClusterSaveConfigCommandCodec.encode(charset = cfg.charset)
    }
    return ClusterSaveConfigCommandCodec.decode(topology.handle(request), cfg.charset)
}
