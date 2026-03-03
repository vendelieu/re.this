package eu.vendeli.rethis.shared.decoders.general

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.ResponseParsingException
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.resolveToken
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.readLineStrict


object BulkStringDecoder : ResponseDecoder<String> {
    override fun decode(input: Buffer, charset: Charset, code: RespCode?): String {
        if (input == EMPTY_BUFFER) return ""
        if (code == null) input.resolveToken(RespCode.BULK)

        val size = input.readLineStrict().toInt()
        if (size < 0) throw ResponseParsingException(
            "Invalid response structure, expected string token got null",
        )
        val output = input.readText(charset, size)
        input.skip(2)

        return output
    }

    fun decodeNullable(input: Buffer, charset: Charset, code: RespCode? = null): String? {
        if (input == EMPTY_BUFFER) return ""
        if (code == null) input.resolveToken(RespCode.BULK)

        val size = input.readLineStrict().toIntOrNull() ?: return null
        if (size < 0) return null

        val output = input.readText(charset, size)
        input.readBytes(2)

        return output
    }
}
