package eu.vendeli.rethis.shared.decoders.generic

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.readResponseWrapped
import eu.vendeli.rethis.shared.utils.resolveToken
import eu.vendeli.rethis.shared.utils.safeCast
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer


object DumpDecoder : ResponseDecoder<ByteArray?> {
    private val EMPTY_BA = ByteArray(0)
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): ByteArray? {
        if (input == EMPTY_BUFFER) return EMPTY_BA
        val code = code ?: input.resolveToken(RespCode.BULK)
        val response = input.readResponseWrapped(Charsets.UTF_8, true, code).safeCast<RType.Raw>()

        return response?.value?.dropLast(2)?.toByteArray() // drop last 2 bytes (CRLF)
    }
}
