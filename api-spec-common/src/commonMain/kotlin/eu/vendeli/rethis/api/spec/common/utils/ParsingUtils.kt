package eu.vendeli.rethis.api.spec.common.utils

import eu.vendeli.rethis.api.spec.common.decoders.general.BulkStringDecoder
import eu.vendeli.rethis.api.spec.common.decoders.general.VerbatimStringDecoder
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.ResponseParsingException
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlin.jvm.JvmName

val EMPTY_BUFFER = Buffer()

@Suppress("UNCHECKED_CAST")
internal inline fun <reified R> Any?.safeCast(): R? = this as? R

@Suppress("UNCHECKED_CAST")
internal inline fun <reified R> Any?.cast(): R = this as R

internal suspend inline fun MutableCollection<String>.parseStrings(size: Int, input: Buffer, charset: Charset) {
    repeat(size) {
        val code = RespCode.fromCode(input.readByte())
        when (code) {
            RespCode.BULK -> add(
                BulkStringDecoder.decode(input, charset, code),
            )

            RespCode.VERBATIM_STRING -> add(
                VerbatimStringDecoder.decode(input, charset, code),
            )

            else -> throw ResponseParsingException(
                "Invalid response structure, expected string token, given $code",
                input.tryInferCause(code),
            )
        }
    }
}

@JvmName("parseStringsNullable")
internal suspend inline fun MutableCollection<String?>.parseStrings(size: Int, input: Buffer, charset: Charset) {
    repeat(size) {
        val code = RespCode.fromCode(input.readByte())
        when (code) {
            RespCode.NULL -> add(null)

            RespCode.BULK -> add(BulkStringDecoder.decodeNullable(input, charset, code))

            RespCode.VERBATIM_STRING -> add(VerbatimStringDecoder.decodeNullable(input, charset, code))

            else -> throw ResponseParsingException(
                "Invalid response structure, expected string token, given $code",
                input.tryInferCause(code),
            )
        }
    }
}

internal inline fun Buffer.resolveToken(requiredToken: RespCode): RespCode {
    val code = RespCode.fromCode(readByte())
    if (code != requiredToken) throw ResponseParsingException(
        "Invalid response structure, expected ${requiredToken.name} token, given $code", tryInferCause(code),
    )

    return code
}
