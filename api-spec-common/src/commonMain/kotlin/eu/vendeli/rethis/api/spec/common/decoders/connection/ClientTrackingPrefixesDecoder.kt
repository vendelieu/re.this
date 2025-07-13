package eu.vendeli.rethis.api.spec.common.decoders.connection

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer

object ClientTrackingPrefixesDecoder : ResponseDecoder<String> {
    override suspend fun decode(input: Buffer, charset: Charset, withCode: Boolean): String {
        return TODO("Not yet implemented")
    }
}
