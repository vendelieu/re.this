package eu.vendeli.rethis.api.spec.common.decoders

import eu.vendeli.rethis.api.spec.common.types.RespCode
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer

object SimpleErrorDecoder : ResponseDecoder<Nothing>(RespCode.SIMPLE_ERROR) {
    override suspend fun decode(input: Buffer, charset: Charset, typeInfo: TypeInfo): Nothing = TODO()
}
