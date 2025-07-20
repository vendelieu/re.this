package eu.vendeli.rethis.api.spec.common.decoders.general

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.ResponseParsingException
import eu.vendeli.rethis.api.spec.common.utils.EMPTY_BUFFER
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLine
import kotlinx.io.readLineStrict


object BulkStringDecoder : ResponseDecoder<String> {
    override suspend fun decode(input: Buffer, charset: Charset, withCode: Boolean): String {
        if (input == EMPTY_BUFFER) return ""
        if (withCode) {
            val code = RespCode.fromCode(input.readByte())
            if (code != RespCode.BULK) throw ResponseParsingException(
                "Invalid response structure, expected bulk string token, given $code",
                input.tryInferCause(code),
            )
        }
        if (input.readLineStrict().toInt() < 0) throw ResponseParsingException(
            "Invalid response structure, expected string token got null",
        )

        return input.readLineStrict()
    }

    suspend fun decodeNullable(input: Buffer, charset: Charset, withCode: Boolean = true): String? {
        if (withCode) {
            val code = RespCode.fromCode(input.readByte())
            if (code != RespCode.BULK) throw ResponseParsingException(
                "Invalid response structure, expected bulk string token, given $code",
                input.tryInferCause(code),
            )
        }
        if (input.readLineStrict().toInt() < 0) return null

        return input.readLine()
    }
}
