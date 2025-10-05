package eu.vendeli.rethis.shared.decoders.server

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.resolveToken
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.readLineStrict


object ShutdownDecoder : ResponseDecoder<Boolean?> {
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): Boolean? {
        if (input == EMPTY_BUFFER) return null
        if (input.remaining == 0L) return null
        if (code == null) input.resolveToken(RespCode.SIMPLE_STRING)

        return input.readLineStrict() == "OK"
    }
}

