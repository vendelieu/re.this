package eu.vendeli.rethis.command.geospatial

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.geospatial.CenterPoint
import eu.vendeli.rethis.api.spec.common.request.geospatial.Shape
import eu.vendeli.rethis.api.spec.common.response.GeoSort
import eu.vendeli.rethis.codecs.geospatial.GeoSearchStoreCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.Long
import kotlin.String

public suspend fun ReThis.geoSearchStore(
    destination: String,
    source: String,
    from: CenterPoint,
    `by`: Shape,
    order: GeoSort? = null,
    count: Long? = null,
    any: Boolean? = null,
    storedist: Boolean? = null,
): Long {
    val request = if(cfg.withSlots) {
        GeoSearchStoreCommandCodec.encodeWithSlot(charset = cfg.charset, destination = destination, source = source, from = from, by = by, order = order, count = count, any = any, storedist = storedist)
    } else {
        GeoSearchStoreCommandCodec.encode(charset = cfg.charset, destination = destination, source = source, from = from, by = by, order = order, count = count, any = any, storedist = storedist)
    }
    return GeoSearchStoreCommandCodec.decode(topology.handle(request), cfg.charset)
}
