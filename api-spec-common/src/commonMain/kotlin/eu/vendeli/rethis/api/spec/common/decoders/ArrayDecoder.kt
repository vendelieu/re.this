package eu.vendeli.rethis.api.spec.common.decoders

import eu.vendeli.rethis.api.spec.common.types.RespCode
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlin.jvm.JvmName

object ArrayDecoder : ResponseDecoder<String>(RespCode.SIMPLE_STRING) {
    override suspend fun decode(input: Buffer, charset: Charset, typeInfo: TypeInfo): String = input.readText(charset)

    @JvmName("decodeList")
    suspend fun <T> decode(input: Buffer, charset: Charset, typeInfo: TypeInfo): List<T> {
        TODO()
    }
}
