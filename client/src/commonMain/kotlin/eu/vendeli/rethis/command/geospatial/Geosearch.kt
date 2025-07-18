package eu.vendeli.rethis.command.geospatial

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.geospatial.CenterPoint
import eu.vendeli.rethis.api.spec.common.request.geospatial.Shape
import eu.vendeli.rethis.api.spec.common.response.GeoSort
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.codecs.geospatial.GeoSearchCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.geoSearch(
    key: String,
    from: CenterPoint,
    `by`: Shape,
    withCoord: Boolean? = null,
    withDist: Boolean? = null,
    withHash: Boolean? = null,
    count: Long? = null,
    any: Boolean? = null,
    order: GeoSort? = null,
): List<RType> {
    val request = if(cfg.withSlots) {
        GeoSearchCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, from = from, by = by, withCoord = withCoord, withDist = withDist, withHash = withHash, count = count, any = any, order = order)
    } else {
        GeoSearchCommandCodec.encode(charset = cfg.charset, key = key, from = from, by = by, withCoord = withCoord, withDist = withDist, withHash = withHash, count = count, any = any, order = order)
    }
    return GeoSearchCommandCodec.decode(topology.handle(request), cfg.charset)
}
