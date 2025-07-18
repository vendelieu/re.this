package eu.vendeli.rethis.command.geospatial

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.request.geospatial.GeoAddOption
import eu.vendeli.rethis.api.spec.common.response.GeoMember
import eu.vendeli.rethis.codecs.geospatial.GeoAddCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.Long
import kotlin.String

public suspend fun ReThis.geoAdd(
    key: String,
    vararg `data`: GeoMember,
    condition: GeoAddOption.UpsertMode? = null,
    change: Boolean? = null,
): Long {
    val request = if(cfg.withSlots) {
        GeoAddCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, data = data, condition = condition, change = change)
    } else {
        GeoAddCommandCodec.encode(charset = cfg.charset, key = key, data = data, condition = condition, change = change)
    }
    return GeoAddCommandCodec.decode(topology.handle(request), cfg.charset)
}
