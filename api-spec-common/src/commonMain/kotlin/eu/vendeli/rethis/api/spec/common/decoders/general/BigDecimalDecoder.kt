package eu.vendeli.rethis.api.spec.common.decoders.general

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.ResponseParsingException
import eu.vendeli.rethis.api.spec.common.utils.EMPTY_BUFFER
import eu.vendeli.rethis.api.spec.common.utils.resolveToken
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLineStrict


object BigDecimalDecoder : ResponseDecoder<BigDecimal> {
    override suspend fun decode(input: Buffer, charset: Charset, code: RespCode?,): BigDecimal {
        if (input == EMPTY_BUFFER) return BigDecimal.ZERO
        if (code == null) input.resolveToken(RespCode.BIG_NUMBER)


        val value = input.readLineStrict()
        return BigDecimal.runCatching { parseString(value) }.onFailure {
            throw ResponseParsingException("Invalid response structure, expected big number, given $value", it)
        }.getOrThrow()
    }
}
