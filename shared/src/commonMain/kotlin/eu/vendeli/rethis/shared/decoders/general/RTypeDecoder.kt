package eu.vendeli.rethis.shared.decoders.general

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.readResponseWrapped
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer

object RTypeDecoder : ResponseDecoder<RType> {
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): RType {
        if (input == EMPTY_BUFFER) return RType.Null
        return input.readResponseWrapped(charset)
    }
}
