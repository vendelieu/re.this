package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.ClusterFailoverCommandCodec
import eu.vendeli.rethis.shared.request.cluster.ClusterFailoverOption
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.clusterFailover(options: ClusterFailoverOption? = null): Boolean {
    val request = if(cfg.withSlots) {
        ClusterFailoverCommandCodec.encodeWithSlot(charset = cfg.charset, options = options)
    } else {
        ClusterFailoverCommandCodec.encode(charset = cfg.charset, options = options)
    }
    return ClusterFailoverCommandCodec.decode(topology.handle(request), cfg.charset)
}
