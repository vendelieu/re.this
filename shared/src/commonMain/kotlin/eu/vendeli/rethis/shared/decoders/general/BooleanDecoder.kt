package eu.vendeli.rethis.shared.decoders.general

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.ResponseParsingException
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.resolveToken
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLineStrict


object BooleanDecoder : ResponseDecoder<Boolean> {
    override suspend fun decode(input: Buffer, charset: Charset, code: RespCode?,): Boolean {
        if (input == EMPTY_BUFFER) return false
        if (code == null) input.resolveToken(RespCode.BOOLEAN)

        return when (val value = input.readLineStrict()) {
            "t" -> true
            "f" -> false
            else -> throw ResponseParsingException("Invalid response structure, expected boolean, given $value")
        }
    }
}
