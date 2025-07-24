package eu.vendeli.rethis.api.spec.common.decoders.general

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.ResponseParsingException
import eu.vendeli.rethis.api.spec.common.utils.EMPTY_BUFFER
import eu.vendeli.rethis.api.spec.common.utils.resolveToken
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLineStrict


object IntegerDecoder : ResponseDecoder<Long> {
    override suspend fun decode(input: Buffer, charset: Charset, code: RespCode?,): Long {
        if (input == EMPTY_BUFFER) return Long.MIN_VALUE
        if (code == null) input.resolveToken(RespCode.INTEGER)

        val value = input.readLineStrict()
        return value.toLongOrNull()
            ?: throw ResponseParsingException("Invalid response structure, expected integer, given $value")
    }
}
