package eu.vendeli.rethis.shared.decoders.general

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.ResponseParsingException
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.EMPTY_BYTE_ARRAY
import eu.vendeli.rethis.shared.utils.resolveToken
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.io.readLine
import kotlinx.io.readLineStrict


object BulkByteArrayDecoder : ResponseDecoder<ByteArray> {
    override suspend fun decode(input: Buffer, charset: Charset, code: RespCode?,): ByteArray {
        if (input == EMPTY_BUFFER) return EMPTY_BYTE_ARRAY
        if (code == null) input.resolveToken(RespCode.BULK)

        val size = input.readLineStrict().toInt()
        if (size < 0) throw ResponseParsingException(
            "Invalid response structure, expected string token got null",
        )

        val output = input.readByteArray(size)
        input.readLine()

        return output
    }

    suspend fun decodeNullable(input: Buffer, charset: Charset, code: RespCode? = null): ByteArray? {
        if (input == EMPTY_BUFFER) return EMPTY_BYTE_ARRAY
        if (code == null) input.resolveToken(RespCode.BULK)

        val size = input.readLineStrict().toIntOrNull() ?: return null
        if (size < 0) return null

        val output = input.readByteArray(size)
        input.readLine()

        return output
    }
}
