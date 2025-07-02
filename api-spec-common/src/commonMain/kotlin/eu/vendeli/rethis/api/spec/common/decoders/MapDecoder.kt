package eu.vendeli.rethis.api.spec.common.decoders

import eu.vendeli.rethis.api.spec.common.types.RespCode
import io.ktor.util.reflect.*
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlin.jvm.JvmName

object MapDecoder : ResponseDecoder<Map<*, *>>() {
    override suspend fun decode(input: Buffer, charset: Charset, typeInfo: TypeInfo): Map<*, *> = TODO()

    @JvmName("decodeMap")
    suspend fun <K : Any, V> decode(input: Buffer, charset: Charset, typeInfo: TypeInfo): Map<K, V> = TODO()
}
