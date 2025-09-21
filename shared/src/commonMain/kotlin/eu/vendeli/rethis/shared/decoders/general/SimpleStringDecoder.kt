package eu.vendeli.rethis.shared.decoders.general

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.resolveToken
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLineStrict


object SimpleStringDecoder : ResponseDecoder<String> {
    override suspend fun decode(input: Buffer, charset: Charset, code: RespCode?,): String {
        if (input == EMPTY_BUFFER) return ""
        if (code == null) input.resolveToken(RespCode.SIMPLE_STRING)

        return input.readLineStrict()
    }

    fun decodeNullable(input: Buffer, charset: Charset, code: RespCode? = null): String? {
        if (input == EMPTY_BUFFER) return ""
        if (code == null) input.resolveToken(RespCode.SIMPLE_STRING)

        return input.readLineStrict()
    }
}
