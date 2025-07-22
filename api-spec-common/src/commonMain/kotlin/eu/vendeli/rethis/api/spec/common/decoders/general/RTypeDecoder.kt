package eu.vendeli.rethis.api.spec.common.decoders.general

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.utils.EMPTY_BUFFER
import eu.vendeli.rethis.api.spec.common.utils.readResponseWrapped
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
