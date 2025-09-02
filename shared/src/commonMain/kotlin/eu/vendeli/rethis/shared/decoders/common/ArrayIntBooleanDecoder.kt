package eu.vendeli.rethis.shared.decoders.common

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.decoders.general.IntegerDecoder
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.resolveToken
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLineStrict

object ArrayIntBooleanDecoder : ResponseDecoder<List<Boolean>> {
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): List<Boolean> {
        if (input == EMPTY_BUFFER) return emptyList()
        if (code == null) input.resolveToken(RespCode.ARRAY)

        val size = input.readLineStrict().toInt()
        if (size == 0) return emptyList()

        return buildList {
            repeat(size) {
                add(IntegerDecoder.decode(input, charset) == 1L)
            }
        }
    }
}
