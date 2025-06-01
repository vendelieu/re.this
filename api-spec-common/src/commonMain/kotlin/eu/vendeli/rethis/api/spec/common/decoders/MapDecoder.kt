package eu.vendeli.rethis.api.spec.common.decoders

import eu.vendeli.rethis.api.spec.common.types.RespCode
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlin.jvm.JvmName

object MapDecoder : ResponseDecoder<Map<*, *>>(RespCode.SIMPLE_STRING) {
    override suspend fun decode(input: Buffer, charset: Charset, typeInfo: TypeInfo): Map<*, *> = TODO()

    @JvmName("decodeMap")
    suspend fun <K : Any, V> decode(input: Buffer, charset: Charset, typeInfo: TypeInfo): Map<K, V> = TODO()
}
