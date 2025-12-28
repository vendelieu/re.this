package eu.vendeli.rethis.shared.decoders.aggregate

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.decoders.general.BulkByteStringDecoder
import eu.vendeli.rethis.shared.decoders.general.BulkStringDecoder
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.ResponseParsingException
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.tryInferCause
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.bytestring.ByteString
import kotlinx.io.readLineStrict

object MapByteStringDecoder : ResponseDecoder<Map<String, ByteString>> {
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): Map<String, ByteString> {
        if (input == EMPTY_BUFFER) return emptyMap()
        val code = code ?: RespCode.fromCode(input.readByte())
        if (code != RespCode.MAP && code != RespCode.ARRAY) throw ResponseParsingException(
            "Invalid response structure, expected map token, given $code", input.tryInferCause(code),
        )

        val size = input.readLineStrict().toInt().let { if (code == RespCode.MAP) it else it / 2 }
        if (size == 0) return emptyMap()

        return buildMap {
            repeat(size) {
                val key = BulkStringDecoder.decode(input, charset)
                val value = BulkByteStringDecoder.decode(input, charset)
                put(key, value)
            }
        }
    }

    suspend fun decodeNullable(
        input: Buffer,
        charset: Charset,
        code: RespCode? = null,
    ): Map<String, ByteString?> {
        if (input == EMPTY_BUFFER) return emptyMap()
        val code = code ?: RespCode.fromCode(input.readByte())
        if (code != RespCode.MAP && code != RespCode.ARRAY) throw ResponseParsingException(
            "Invalid response structure, expected map token, given $code", input.tryInferCause(code),
        )

        val size = input.readLineStrict().toInt().let { if (code == RespCode.MAP) it else it / 2 }
        if (size == 0) return emptyMap()

        return buildMap {
            repeat(size) {
                val key = BulkStringDecoder.decodeNullable(input, charset) ?: throw ResponseParsingException(
                    "Invalid response structure, expected string token got null",
                    input.tryInferCause(code),
                )
                val value = BulkByteStringDecoder.decodeNullable(input, charset)
                put(key, value)
            }
        }
    }
}
