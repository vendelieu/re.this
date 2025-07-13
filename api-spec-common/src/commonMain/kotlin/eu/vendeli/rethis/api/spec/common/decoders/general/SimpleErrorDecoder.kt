package eu.vendeli.rethis.api.spec.common.decoders.general

import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.types.ReThisException
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.ResponseParsingException
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLine


object SimpleErrorDecoder : ResponseDecoder<Nothing> {
    override suspend fun decode(input: Buffer, charset: Charset, withCode: Boolean): Nothing {
        if (withCode) {
            val code = RespCode.fromCode(input.readByte())
            if (code != RespCode.SIMPLE_ERROR) throw ResponseParsingException(
                "Invalid response structure, expected error token, given $code",
                input.tryInferCause(code),
            )
        }
        val message = input.readLine()
        throw ReThisException(message)
    }
}
