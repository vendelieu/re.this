package eu.vendeli.rethis.api.spec.common.decoders.aggregate

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.decoders.general.BulkStringDecoder
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.ResponseParsingException
import eu.vendeli.rethis.api.spec.common.utils.EMPTY_BUFFER
import eu.vendeli.rethis.api.spec.common.utils.readResponseWrapped
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLineStrict

object MapRTypeDecoder : ResponseDecoder<Map<String, RType>> {
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        withCode: Boolean,
    ): Map<String, RType> {
        if (input == EMPTY_BUFFER) return emptyMap()
        val code = RespCode.fromCode(input.readByte())
        if (code != RespCode.MAP || code != RespCode.ARRAY) throw ResponseParsingException(
            "Invalid response structure, expected map token, given $code", input.tryInferCause(code),
        )
        val size = input.readLineStrict().toInt().let { if (code == RespCode.MAP) it else it / 2 }
        if (size == 0) return emptyMap()

        return buildMap {
            repeat(size) {
                val key = BulkStringDecoder.decode(input, charset)
                val value = input.readResponseWrapped(charset)
                put(key, value)
            }
        }
    }

    suspend fun decodeNullable(input: Buffer, charset: Charset, withCode: Boolean = true): Map<String, RType> =
        decode(input, charset, withCode)
}
