package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.codecs.cluster.ClusterLinksCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.collections.List

public suspend fun ReThis.clusterLinks(): List<RType> {
    val request = if(cfg.withSlots) {
        ClusterLinksCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        ClusterLinksCommandCodec.encode(charset = cfg.charset, )
    }
    return ClusterLinksCommandCodec.decode(topology.handle(request), cfg.charset)
}
