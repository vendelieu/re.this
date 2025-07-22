package eu.vendeli.rethis.api.spec.common.decoders.general

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.types.ReThisException
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.ResponseParsingException
import eu.vendeli.rethis.api.spec.common.utils.EMPTY_BUFFER
import eu.vendeli.rethis.api.spec.common.utils.resolveToken
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
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
