package eu.vendeli.rethis.api.spec.common.decoders.connection

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.charsets.Charset
import kotlinx.io.Buffer

object ClientTrackingPrefixesDecoder : ResponseDecoder<String> {
    override suspend fun decode(input: Buffer, charset: Charset, typeInfo: TypeInfo): String {
        return TODO("Not yet implemented")
    }
}
