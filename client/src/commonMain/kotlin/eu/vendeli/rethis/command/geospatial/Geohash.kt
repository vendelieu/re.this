package eu.vendeli.rethis.command.geospatial

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.geospatial.GeoHashCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.geoHash(key: String, vararg member: String): List<String> {
    val request = if(cfg.withSlots) {
        GeoHashCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, member = member)
    } else {
        GeoHashCommandCodec.encode(charset = cfg.charset, key = key, member = member)
    }
    return GeoHashCommandCodec.decode(topology.handle(request), cfg.charset)
}
