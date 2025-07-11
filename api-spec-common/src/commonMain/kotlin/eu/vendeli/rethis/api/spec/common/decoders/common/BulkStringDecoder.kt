package eu.vendeli.rethis.api.spec.common.decoders.common

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.ResponseParsingException
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import io.ktor.util.reflect.*
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLine
import kotlinx.io.readLineStrict

object BulkStringDecoder : ResponseDecoder<String> {
    override suspend fun decode(input: Buffer, charset: Charset, withCode: Boolean): String {
        if (withCode) {
            val code = RespCode.fromCode(input.readByte())
            if (code != RespCode.BULK) throw ResponseParsingException(
                "Invalid response structure, expected bulk string token, given $code",
                input.tryInferCause(code),
            )
        }
        input.readLine() // skip size
        return input.readLineStrict()
    }
}
