package eu.vendeli.rethis.api.spec.common.decoders

import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer

interface ResponseDecoder<T : Any> {
    suspend fun decode(input: Buffer, charset: Charset, withCode: Boolean = true): T
}
