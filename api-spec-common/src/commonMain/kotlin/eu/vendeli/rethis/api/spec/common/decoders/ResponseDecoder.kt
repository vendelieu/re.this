package eu.vendeli.rethis.api.spec.common.decoders

import eu.vendeli.rethis.api.spec.common.types.RespCode
import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*

sealed class ResponseDecoder<T : Any>(val type: RespCode) {
    abstract suspend fun decode(input: ByteReadChannel, charset: Charset): T
}
