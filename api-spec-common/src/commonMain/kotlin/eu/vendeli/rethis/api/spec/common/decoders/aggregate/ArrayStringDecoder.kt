package eu.vendeli.rethis.api.spec.common.decoders.aggregate

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.utils.EMPTY_BUFFER
import eu.vendeli.rethis.api.spec.common.utils.parseStrings
import eu.vendeli.rethis.api.spec.common.utils.resolveToken
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLineStrict

object ArrayStringDecoder : ResponseDecoder<List<String>> {
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): List<String> {
        if (input == EMPTY_BUFFER) return emptyList()
        if (code == null) input.resolveToken(RespCode.ARRAY)

        val size = input.readLineStrict().toInt()
        if (size == 0) return emptyList()

        return buildList { parseStrings(size, input, charset) }
    }

    suspend fun decodeNullable(
        input: Buffer,
        charset: Charset,
        code: RespCode? = null,
    ): List<String?> {
        if (input == EMPTY_BUFFER) return emptyList()
        if (code == null) input.resolveToken(RespCode.ARRAY)

        val size = input.readLineStrict().toInt()
        if (size == 0) return emptyList()

        return buildList { parseStrings(size, input, charset) }
    }
}
