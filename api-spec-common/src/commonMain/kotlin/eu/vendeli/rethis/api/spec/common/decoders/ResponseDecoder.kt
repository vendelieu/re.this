package eu.vendeli.rethis.api.spec.common.decoders

import eu.vendeli.rethis.api.spec.common.types.RespCode
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer

interface ResponseDecoder<T> {
    suspend fun decode(input: Buffer, charset: Charset, code: RespCode? = null): T
}
