package eu.vendeli.rethis.api.spec.common.decoders.general

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.ResponseParsingException
import eu.vendeli.rethis.api.spec.common.utils.EMPTY_BUFFER
import eu.vendeli.rethis.api.spec.common.utils.resolveToken
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLineStrict


object DoubleDecoder : ResponseDecoder<Double> {
    override suspend fun decode(input: Buffer, charset: Charset, code: RespCode?): Double {
        if (input == EMPTY_BUFFER) return Double.NaN
        if (code == null) input.resolveToken(RespCode.DOUBLE)

        val value = input.readLineStrict()
        return value.toDoubleOrNull()
            ?: throw ResponseParsingException("Invalid response structure, expected double, given $value")
    }
}
