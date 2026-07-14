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


object BulkStringDecoder : ResponseDecoder<String> {
    override fun decode(input: Buffer, charset: Charset, code: RespCode?): String {
        if (input == EMPTY_BUFFER) return ""
        if (code == null) input.resolveToken(RespCode.BULK)

        val size = input.readDecimalCrlf().toInt()
        if (size < 0) throw ResponseParsingException(
            "Invalid response structure, expected string token got null",
        )

        return input.readSizedText(size, charset)
    }

    fun decodeNullable(input: Buffer, charset: Charset, code: RespCode? = null): String? {
        if (input == EMPTY_BUFFER) return ""
        if (code == null) input.resolveToken(RespCode.BULK)

        val size = input.readDecimalCrlf().toInt()
        if (size < 0) return null

        return input.readSizedText(size, charset)
    }
}
