package eu.vendeli.rethis.api.spec.common.decoders.common

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.ResponseParsingException
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import io.ktor.util.reflect.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.readLineStrict

object SimpleStringDecoder : ResponseDecoder<String> {
    override suspend fun decode(input: Buffer, charset: Charset, withCode: Boolean): String {
        if (withCode) {
            val code = RespCode.fromCode(input.readByte())
            if (code != RespCode.SIMPLE_STRING) throw ResponseParsingException(
                "Invalid response structure, expected simple string token, given $code",
                input.tryInferCause(code),
            )
        }
        return input.readLineStrict()
    }
}
