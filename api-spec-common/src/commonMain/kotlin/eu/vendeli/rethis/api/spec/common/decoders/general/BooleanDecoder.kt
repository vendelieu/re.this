package eu.vendeli.rethis.api.spec.common.decoders.general

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.ResponseParsingException
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLineStrict


object BooleanDecoder : ResponseDecoder<Boolean> {
    override suspend fun decode(input: Buffer, charset: Charset, withCode: Boolean): Boolean {
        if (withCode) {
            val code = RespCode.fromCode(input.readByte())
            if (code != RespCode.BOOLEAN) throw ResponseParsingException(
                "Invalid response structure, expected boolean token, given $code",
                input.tryInferCause(code),
            )
        }

        val value = input.readLineStrict()
        return when (value) {
            "t" -> true
            "f" -> false
            else -> throw ResponseParsingException("Invalid response structure, expected boolean, given $value")
        }
    }
}
