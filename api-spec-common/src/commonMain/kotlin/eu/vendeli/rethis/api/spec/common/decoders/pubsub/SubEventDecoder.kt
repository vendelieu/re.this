package eu.vendeli.rethis.api.spec.common.decoders.pubsub

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.ResponseParsingException
import eu.vendeli.rethis.api.spec.common.utils.EMPTY_BUFFER
import eu.vendeli.rethis.api.spec.common.utils.parseStrings
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLineStrict

object SubEventDecoder : ResponseDecoder<List<String>> {
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): List<String> {
        if (input == EMPTY_BUFFER) return emptyList()
        val code = code ?: RespCode.fromCode(input.readByte())
        if (code != RespCode.ARRAY || code != RespCode.PUSH) throw ResponseParsingException(
            "Invalid response structure, expected array/push token, given $code", input.tryInferCause(code),
        )

        val size = input.readLineStrict().toInt()
        if (size == 0) return emptyList()

        return buildList { parseStrings(size, input, charset) }
    }
}
