package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.request.cluster.ClusterResetMode
import eu.vendeli.rethis.codecs.cluster.ClusterResetCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.clusterReset(resetType: ClusterResetMode? = null): Boolean {
    val request = if(cfg.withSlots) {
        ClusterResetCommandCodec.encodeWithSlot(charset = cfg.charset, resetType = resetType)
    } else {
        ClusterResetCommandCodec.encode(charset = cfg.charset, resetType = resetType)
    }
    return ClusterResetCommandCodec.decode(topology.handle(request), cfg.charset)
}
