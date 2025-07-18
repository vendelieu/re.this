package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.ClusterInfoCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String

public suspend fun ReThis.clusterInfo(): String {
    val request = if(cfg.withSlots) {
        ClusterInfoCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        ClusterInfoCommandCodec.encode(charset = cfg.charset, )
    }
    return ClusterInfoCommandCodec.decode(topology.handle(request), cfg.charset)
}
