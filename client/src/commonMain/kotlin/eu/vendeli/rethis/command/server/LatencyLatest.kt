package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.codecs.server.LatencyLatestCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.latencyLatest(): List<RType> {
    val request = if(cfg.withSlots) {
        LatencyLatestCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        LatencyLatestCommandCodec.encode(charset = cfg.charset, )
    }
    return LatencyLatestCommandCodec.decode(topology.handle(request), cfg.charset)
}
