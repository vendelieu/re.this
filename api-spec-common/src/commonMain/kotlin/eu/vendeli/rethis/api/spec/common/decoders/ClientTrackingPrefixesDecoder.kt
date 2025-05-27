package eu.vendeli.rethis.api.spec.common.decoders

import eu.vendeli.rethis.api.spec.common.types.RespCode
import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer

object ClientTrackingPrefixesDecoder : ResponseDecoder<String>(RespCode.SIMPLE_STRING) {
    override suspend fun decode(input: Buffer, charset: Charset): String {
        return TODO("Not yet implemented")
    }
}
