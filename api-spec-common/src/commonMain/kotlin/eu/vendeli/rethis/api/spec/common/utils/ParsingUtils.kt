package eu.vendeli.rethis.api.spec.common.utils

import eu.vendeli.rethis.api.spec.common.decoders.common.BulkStringDecoder
import eu.vendeli.rethis.api.spec.common.decoders.common.VerbatimStringDecoder
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.ResponseParsingException
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer

internal suspend inline fun MutableCollection<String>.parseStrings(size: Int, input: Buffer, charset: Charset) {
    repeat(size) {
        val code = RespCode.fromCode(input.readByte())
        when (code) {
            RespCode.BULK -> add(BulkStringDecoder.decode(input, charset, false))

            RespCode.VERBATIM_STRING -> add(VerbatimStringDecoder.decode(input, charset, false))

            else -> throw ResponseParsingException(
                "Invalid response structure, expected string token, given $code",
                input.tryInferCause(code),
            )
        }
    }
}
