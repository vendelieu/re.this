package eu.vendeli.rethis.shared.decoders.general

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.types.ReThisException
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.resolveToken
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLine


object SimpleErrorDecoder : ResponseDecoder<Nothing> {
    override suspend fun decode(input: Buffer, charset: Charset, code: RespCode?,): Nothing {
        if (input == EMPTY_BUFFER) throw NotImplementedError()
        if (code == null) input.resolveToken(RespCode.SIMPLE_ERROR)

        val message = input.readLine()
        throw ReThisException(message)
    }
}
