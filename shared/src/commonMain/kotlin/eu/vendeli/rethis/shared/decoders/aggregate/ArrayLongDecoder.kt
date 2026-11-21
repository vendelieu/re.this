package eu.vendeli.rethis.shared.decoders.aggregate

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.decoders.general.IntegerDecoder
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.ResponseParsingException
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.readDecimalCrlf
import eu.vendeli.rethis.shared.utils.resolveToken
import eu.vendeli.rethis.shared.utils.tryInferCause
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer

object ArrayLongDecoder : ResponseDecoder<List<Long>> {
    override fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): List<Long> {
        if (input == EMPTY_BUFFER) return emptyList()
        if (code == null) input.resolveToken(RespCode.ARRAY)

        val size = input.readDecimalCrlf().toInt()
        if (size == 0) return emptyList()

        return buildList {
            repeat(size) { add(IntegerDecoder.decode(input, charset)) }
        }
    }

    fun decodeNullable(
        input: Buffer,
        charset: Charset,
        code: RespCode? = null,
    ): List<Long?> {
        if (input == EMPTY_BUFFER) return emptyList()
        if (code == null) input.resolveToken(RespCode.ARRAY)

        val size = input.readDecimalCrlf().toInt()
        if (size == 0) return emptyList()

        return buildList {
            repeat(size) { add(decodeNullableElement(input, charset)) }
        }
    }

    private fun decodeNullableElement(input: Buffer, charset: Charset): Long? =
        when (val code = RespCode.fromCode(input.readByte())) {
            RespCode.NULL -> {
                input.skip(2) // trailing CRLF of the null marker
                null
            }

            RespCode.BULK -> {
                // RESP2 null bulk string (`$-1`)
                if (input.readDecimalCrlf() >= 0) throw ResponseParsingException(
                    "Invalid response structure, expected integer or null token, given $code",
                )
                null
            }

            RespCode.INTEGER -> IntegerDecoder.decode(input, charset, code)

            else -> throw input.tryInferCause(code, charset) ?: ResponseParsingException(
                "Invalid response structure, expected integer or null token, given $code",
            )
        }
}
