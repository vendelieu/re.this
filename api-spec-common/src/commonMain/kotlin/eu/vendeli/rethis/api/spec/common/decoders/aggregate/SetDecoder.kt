package eu.vendeli.rethis.api.spec.common.decoders.aggregate

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import io.ktor.util.reflect.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlin.jvm.JvmName

object SetDecoder : ResponseDecoder<String>() {
    override suspend fun decode(input: Buffer, charset: Charset, typeInfo: TypeInfo): String = input.readText(charset)

    @JvmName("decodeList")
    suspend inline fun <reified T> decode(input: Buffer, charset: Charset, typeInfo: TypeInfo): Set<T> {
        TODO()
    }
}
