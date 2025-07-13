package eu.vendeli.rethis.api.spec.common.decoders.common

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.ResponseParsingException
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLineStrict

object DoubleDecoder : ResponseDecoder<Double> {
    override suspend fun decode(input: Buffer, charset: Charset, withCode: Boolean): Double {
        if (withCode) {
            val code = RespCode.fromCode(input.readByte())
            if (code != RespCode.DOUBLE) throw ResponseParsingException(
                "Invalid response structure, expected double token, given $code",
                input.tryInferCause(code),
            )
        }

        val value = input.readLineStrict()
        return value.toDoubleOrNull()
            ?: throw ResponseParsingException("Invalid response structure, expected double, given $value")
    }
}
