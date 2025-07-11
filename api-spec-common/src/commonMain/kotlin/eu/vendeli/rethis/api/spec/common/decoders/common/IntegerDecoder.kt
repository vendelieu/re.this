package eu.vendeli.rethis.api.spec.common.decoders.common

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.ResponseParsingException
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import io.ktor.util.reflect.*
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLineStrict

object IntegerDecoder : ResponseDecoder<Long> {
    override suspend fun decode(input: Buffer, charset: Charset, withCode: Boolean): Long {
        if (withCode) {
            val code = RespCode.fromCode(input.readByte())
            if (code != RespCode.INTEGER) throw ResponseParsingException(
                "Invalid response structure, expected integer token, given $code",
                input.tryInferCause(code),
            )
        }
        val value = input.readLineStrict()
        return value.toLongOrNull()
            ?: throw ResponseParsingException("Invalid response structure, expected integer, given $value")
    }
}
