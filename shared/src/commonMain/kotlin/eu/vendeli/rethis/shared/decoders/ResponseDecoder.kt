package eu.vendeli.rethis.shared.decoders

import eu.vendeli.rethis.shared.types.RespCode
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer

interface ResponseDecoder<T> {
    fun decode(input: Buffer, charset: Charset, code: RespCode? = null): T
}
