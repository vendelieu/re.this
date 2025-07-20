package eu.vendeli.rethis.api.spec.common.decoders.general

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.types.ReThisException
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.ResponseParsingException
import eu.vendeli.rethis.api.spec.common.utils.EMPTY_BUFFER
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLineStrict


object BulkErrorDecoder : ResponseDecoder<Nothing> {
    override suspend fun decode(input: Buffer, charset: Charset, withCode: Boolean): Nothing {
        if (input == EMPTY_BUFFER) throw NotImplementedError()
        if (withCode) {
            val code = RespCode.fromCode(input.readByte())
            if (code != RespCode.BULK_ERROR) throw ResponseParsingException(
                "Invalid response structure, expected bulk error token, given $code",
                input.tryInferCause(code),
            )
        }
        val message = StringBuilder()

        val size = input.readLineStrict().toInt()
        repeat(size) {
            message.appendLine(BulkStringDecoder.decode(input, charset))
        }

        throw ReThisException(message.toString())
    }
}
