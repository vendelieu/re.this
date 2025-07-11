package eu.vendeli.rethis.api.spec.common.decoders.string

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.charsets.Charset
import kotlinx.io.Buffer

object LcsDecoder : ResponseDecoder<String>() {
    override suspend fun decode(input: Buffer, charset: Charset, typeInfo: TypeInfo): String = TODO()
}
