package eu.vendeli.rethis.shared.decoders.aggregate

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.parseStrings
import eu.vendeli.rethis.shared.utils.resolveToken
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLineStrict

object SetStringDecoder : ResponseDecoder<Set<String>> {
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): Set<String> {
        if (input == EMPTY_BUFFER) return emptySet()
        if (code == null) input.resolveToken(RespCode.SET)

        val size = input.readLineStrict().toInt()
        if (size == 0) return emptySet()

        return buildSet { parseStrings(size, input, charset) }
    }
}
