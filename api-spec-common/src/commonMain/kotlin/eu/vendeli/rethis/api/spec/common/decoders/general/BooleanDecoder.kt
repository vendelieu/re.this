package eu.vendeli.rethis.api.spec.common.decoders.general

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.ResponseParsingException
import eu.vendeli.rethis.api.spec.common.utils.EMPTY_BUFFER
import eu.vendeli.rethis.api.spec.common.utils.resolveToken
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLineStrict


object BooleanDecoder : ResponseDecoder<Boolean> {
    override suspend fun decode(input: Buffer, charset: Charset, code: RespCode?,): Boolean {
        if (input == EMPTY_BUFFER) return false
        if (code == null) input.resolveToken(RespCode.BOOLEAN)


        val value = input.readLineStrict()
        return when (value) {
            "t" -> true
            "f" -> false
            else -> throw ResponseParsingException("Invalid response structure, expected boolean, given $value")
        }
    }
}
