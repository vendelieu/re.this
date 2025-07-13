package eu.vendeli.rethis.api.spec.common.decoders.general

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.ResponseParsingException
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLineStrict


object BigDecimalDecoder : ResponseDecoder<BigDecimal> {
    override suspend fun decode(input: Buffer, charset: Charset, withCode: Boolean): BigDecimal {
        if (withCode) {
            val code = RespCode.fromCode(input.readByte())
            if (code != RespCode.BIG_NUMBER) throw ResponseParsingException(
                "Invalid response structure, expected big number token, given $code",
                input.tryInferCause(code),
            )
        }

        val value = input.readLineStrict()
        return BigDecimal.runCatching { parseString(value) }.onFailure {
            throw ResponseParsingException("Invalid response structure, expected big number, given $value", it)
        }.getOrThrow()
    }
}
