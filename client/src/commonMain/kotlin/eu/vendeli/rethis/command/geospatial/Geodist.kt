package eu.vendeli.rethis.command.geospatial

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.response.GeoUnit
import eu.vendeli.rethis.codecs.geospatial.GeoDistCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Double
import kotlin.String

public suspend fun ReThis.geoDist(
    key: String,
    member1: String,
    member2: String,
    unit: GeoUnit? = null,
): Double? {
    val request = if(cfg.withSlots) {
        GeoDistCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, member1 = member1, member2 = member2, unit = unit)
    } else {
        GeoDistCommandCodec.encode(charset = cfg.charset, key = key, member1 = member1, member2 = member2, unit = unit)
    }
    return GeoDistCommandCodec.decode(topology.handle(request), cfg.charset)
}
