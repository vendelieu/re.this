package eu.vendeli.rethis.shared.decoders.general

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.ResponseParsingException
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.EMPTY_BYTE_ARRAY
import eu.vendeli.rethis.shared.utils.readDecimalCrlf
import eu.vendeli.rethis.shared.utils.resolveToken
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readByteArray


object BulkByteArrayDecoder : ResponseDecoder<ByteArray> {
    override fun decode(input: Buffer, charset: Charset, code: RespCode?,): ByteArray {
        if (input == EMPTY_BUFFER) return EMPTY_BYTE_ARRAY
        if (code == null) input.resolveToken(RespCode.BULK)

        val size = input.readDecimalCrlf().toInt()
        if (size < 0) throw ResponseParsingException(
            "Invalid response structure, expected string token got null",
        )

        val output = input.readByteArray(size)
        input.skip(2) // trailing CRLF

        return output
    }

    fun decodeNullable(input: Buffer, charset: Charset, code: RespCode? = null): ByteArray? {
        if (input == EMPTY_BUFFER) return EMPTY_BYTE_ARRAY
        if (code == null) input.resolveToken(RespCode.BULK)

        val size = input.readDecimalCrlf().toInt()
        if (size < 0) return null

        val output = input.readByteArray(size)
        input.skip(2) // trailing CRLF

        return output
    }
}
