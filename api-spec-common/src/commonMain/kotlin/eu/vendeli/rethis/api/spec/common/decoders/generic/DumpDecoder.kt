package eu.vendeli.rethis.api.spec.common.decoders.generic

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.utils.EMPTY_BUFFER
import eu.vendeli.rethis.api.spec.common.utils.readResponseWrapped
import eu.vendeli.rethis.api.spec.common.utils.resolveToken
import eu.vendeli.rethis.api.spec.common.utils.safeCast
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

        return response?.value
    }
}
