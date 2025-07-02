package eu.vendeli.rethis.api.spec.common.decoders

import io.ktor.util.reflect.*
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer

sealed class ResponseDecoder<T : Any>() {
    abstract suspend fun decode(input: Buffer, charset: Charset, typeInfo: TypeInfo): T
}
