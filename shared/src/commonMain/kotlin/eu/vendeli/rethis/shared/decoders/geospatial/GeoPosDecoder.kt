package eu.vendeli.rethis.shared.decoders.geospatial

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.decoders.aggregate.ArrayRTypeDecoder
import eu.vendeli.rethis.shared.response.geospatial.GeoPosition
import eu.vendeli.rethis.shared.types.RArray
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.safeCast
import eu.vendeli.rethis.shared.utils.unwrap
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer


object GeoPosDecoder : ResponseDecoder<List<List<GeoPosition>?>> {
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): List<List<GeoPosition>?> {
        if (input == EMPTY_BUFFER) return emptyList()
        return ArrayRTypeDecoder.decode(input, charset).map { entry ->
            entry.safeCast<RArray>()?.value?.chunked(2) {
                GeoPosition(
                    it.first().unwrap<String>()!!.toDouble(),
                    it.last().unwrap<String>()!!.toDouble()
                )
            }
        }
    }
}
