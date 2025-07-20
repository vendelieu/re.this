package eu.vendeli.rethis.api.spec.common.decoders.geospatial

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.decoders.aggregate.ArrayRTypeDecoder
import eu.vendeli.rethis.api.spec.common.response.geospatial.GeoPosition
import eu.vendeli.rethis.api.spec.common.types.RArray
import eu.vendeli.rethis.api.spec.common.utils.EMPTY_BUFFER
import eu.vendeli.rethis.api.spec.common.utils.safeCast
import eu.vendeli.rethis.api.spec.common.utils.unwrap
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer


object GeoPosDecoder : ResponseDecoder<List<List<GeoPosition>?>> {
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        withCode: Boolean,
    ): List<List<GeoPosition>?> {
        if (input == EMPTY_BUFFER) return emptyList()
        return ArrayRTypeDecoder.decode(input, charset).map { entry ->
            entry.safeCast<RArray>()?.value?.chunked(2) {
                GeoPosition(it.first().unwrap()!!, it.last().unwrap()!!)
            }
        }
    }
}
