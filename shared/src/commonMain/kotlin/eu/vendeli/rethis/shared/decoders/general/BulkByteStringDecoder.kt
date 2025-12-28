package eu.vendeli.rethis.shared.decoders.general

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.ResponseParsingException
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.EMPTY_BYTE_STRING
import eu.vendeli.rethis.shared.utils.resolveToken
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.readFully
import kotlinx.io.Buffer
import kotlinx.io.bytestring.ByteString
import kotlinx.io.readByteString
import kotlinx.io.readLine
import kotlinx.io.readLineStrict


object BulkByteStringDecoder : ResponseDecoder<ByteString> {
    override suspend fun decode(input: Buffer, charset: Charset, code: RespCode?,): ByteString {
        if (input == EMPTY_BUFFER) return EMPTY_BYTE_STRING
        if (code == null) input.resolveToken(RespCode.BULK)

        val size = input.readLineStrict().toInt()
        if (size < 0) throw ResponseParsingException(
            "Invalid response structure, expected string token got null",
        )

        val output = input.readByteString(size)
        input.readLine()

        return output
    }

    suspend fun decodeNullable(input: Buffer, charset: Charset, code: RespCode? = null): ByteString? {
        if (input == EMPTY_BUFFER) return EMPTY_BYTE_STRING
        if (code == null) input.resolveToken(RespCode.BULK)

        val size = input.readLineStrict().toIntOrNull() ?: return null
        if (size < 0) return null

        val output = input.readByteString(size)
        input.readLine()

        return output
    }
}
