package eu.vendeli.rethis.shared.decoders.general

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.ResponseParsingException
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.readDecimalCrlf
import eu.vendeli.rethis.shared.utils.readSizedText
import eu.vendeli.rethis.shared.utils.resolveToken
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer

private const val VERBATIM_PREFIX_LENGTH = 4

object VerbatimStringDecoder : ResponseDecoder<String> {
    override fun decode(input: Buffer, charset: Charset, code: RespCode?,): String {
        if (input == EMPTY_BUFFER) return ""
        if (code == null) input.resolveToken(RespCode.VERBATIM_STRING)

        val size = input.readDecimalCrlf().toInt()
        if (size < 0) throw ResponseParsingException(
            "Invalid response structure, expected string token got null",
        )

        return input.readVerbatimContent(size, charset)
    }

    fun decodeNullable(input: Buffer, charset: Charset, code: RespCode?,): String? {
        if (input == EMPTY_BUFFER) return null
        if (code == null) input.resolveToken(RespCode.VERBATIM_STRING)

        val size = input.readDecimalCrlf().toInt()
        if (size < 0) return null

        return input.readVerbatimContent(size, charset)
    }

    private fun Buffer.readVerbatimContent(size: Int, charset: Charset): String {
        if (size < VERBATIM_PREFIX_LENGTH) throw ResponseParsingException(
            "Invalid verbatim string length $size, expected at least $VERBATIM_PREFIX_LENGTH",
        )
        skip(VERBATIM_PREFIX_LENGTH.toLong()) // 3-byte format and ':'
        return readSizedText(size - VERBATIM_PREFIX_LENGTH, charset)
    }
}
