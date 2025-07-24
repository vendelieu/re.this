package eu.vendeli.rethis.command.geospatial

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.response.geospatial.GeoPosition
import eu.vendeli.rethis.codecs.geospatial.GeoPosCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.geoPos(key: String, vararg member: String): List<List<GeoPosition>?> {
    val request = if(cfg.withSlots) {
        GeoPosCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, member = member)
    } else {
        GeoPosCommandCodec.encode(charset = cfg.charset, key = key, member = member)
    }
    return GeoPosCommandCodec.decode(topology.handle(request), cfg.charset)
}
