package eu.vendeli.rethis.api.spec.common.decoders.aggregate

import eu.vendeli.rethis.api.spec.common.annotations.RedisMeta
import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.ResponseParsingException
import eu.vendeli.rethis.api.spec.common.utils.parseStrings
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLineStrict

object ArrayStringDecoder : ResponseDecoder<List<String>> {
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        withCode: Boolean,
    ): List<String> {
        if (withCode) {
            val code = RespCode.fromCode(input.readByte())
            if (code != RespCode.ARRAY) throw ResponseParsingException(
                "Invalid response structure, expected array token, given $code", input.tryInferCause(code),
            )
        }
        val size = input.readLineStrict().toInt()
        if (size == 0) return emptyList()

        return buildList { parseStrings(size, input, charset) }
    }

    suspend fun decodeNullable(
        input: Buffer,
        charset: Charset,
        withCode: Boolean = true,
    ): List<String?> {
        if (withCode) {
            val code = RespCode.fromCode(input.readByte())
            if (code != RespCode.ARRAY) throw ResponseParsingException(
                "Invalid response structure, expected array token, given $code", input.tryInferCause(code),
            )
        }
        val size = input.readLineStrict().toInt()
        if (size == 0) return emptyList()

        return buildList { parseStrings(size, input, charset) }
    }
}
