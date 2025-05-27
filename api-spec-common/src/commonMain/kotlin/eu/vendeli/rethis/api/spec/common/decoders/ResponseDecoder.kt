package eu.vendeli.rethis.api.spec.common.decoders

import eu.vendeli.rethis.api.spec.common.types.RespCode
import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer

sealed class ResponseDecoder<T : Any>(val type: RespCode) {
    abstract suspend fun decode(input: Buffer, charset: Charset): T
}
