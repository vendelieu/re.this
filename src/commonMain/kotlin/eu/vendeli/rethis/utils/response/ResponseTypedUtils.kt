package eu.vendeli.rethis.utils.response

import com.ionspin.kotlin.bignum.integer.BigInteger
import eu.vendeli.rethis.RedisError
import eu.vendeli.rethis.ResponseParsingException
import eu.vendeli.rethis.types.core.RespCode
import eu.vendeli.rethis.types.core.ResponseToken
import eu.vendeli.rethis.utils.Const.FALSE_BYTE
import eu.vendeli.rethis.utils.Const.TRUE_BYTE
import eu.vendeli.rethis.utils.safeCast
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlinx.io.readDecimalLong
import kotlinx.io.readString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
private inline fun <T : Any> Source.decodeFromSource(
    tClass: KClass<T>,
    charset: Charset,
    jsonModule: Json? = null,
) = when {
    tClass == String::class -> readText(charset)
    jsonModule != null -> jsonModule.decodeFromSource(tClass.serializer(), this)
    else -> readText(charset)
}.safeCast(tClass)

@Throws(RedisError::class, ResponseParsingException::class)
internal fun <T : Any> ArrayDeque<ResponseToken>.readSimpleResponseTyped(
    tClass: KClass<T>,
    charset: Charset,
    jsonModule: Json? = null,
): T? {
    if (isEmpty()) return null
    val typeToken = validatedResponseType()
    val size = typeToken.size ?: 0
    if (size == -1) return null
    val data = validatedSimpleResponse(typeToken)

    return when (typeToken.code) {
        RespCode.SIMPLE_STRING -> data.decodeFromSource(tClass, charset, jsonModule)
        RespCode.SIMPLE_ERROR -> throw RedisError(data.readText(charset))
        RespCode.INTEGER -> data.readDecimalLong()
        RespCode.BULK -> {
            if (size < 0) return null
            data.decodeFromSource(tClass, charset, jsonModule)
        }

        RespCode.BOOLEAN -> when (val line = data.readByte()) {
            TRUE_BYTE -> true
            FALSE_BYTE -> false
            else -> throw ResponseParsingException(message = "Invalid boolean format: $line")
        }

        RespCode.DOUBLE -> data.readString().toDouble()

        RespCode.BIG_NUMBER -> try {
            BigInteger.parseString(data.readText(charset))
        } catch (e: NumberFormatException) {
            throw ResponseParsingException(message = "Invalid BigInteger format", cause = e)
        }

        RespCode.BULK_ERROR -> {
            if (size < 0) throw ResponseParsingException(message = "Invalid bulk error size: $size")
            throw RedisError(message = data.readText(charset), isBulk = true)
        }

        RespCode.VERBATIM_STRING -> {
            if (size < 0) return null
            val encodingBytes = data.readByteArray(3)
            return if (jsonModule == null) {
                val encoding = encodingBytes.decodeToString()
                val content = data.readText(charset)

                "$encoding:$content"
            } else {
                data.decodeFromSource(tClass, charset, jsonModule)
            }?.safeCast(tClass)
        }

        else -> null
    }?.safeCast(tClass)
}

internal fun <T : Any> ArrayDeque<ResponseToken>.readListResponseTyped(
    tClass: KClass<T>,
    charset: Charset,
    jsonModule: Json? = null,
): List<T>? {
    if (isEmpty()) return null
    val typeToken = validatedResponseType()
    val size = typeToken.size ?: 0

    return when (typeToken.code) {
        RespCode.ARRAY, RespCode.SET, RespCode.PUSH -> {
            if (size < 0) return null
            List(size) {
                readSimpleResponseTyped<T>(tClass, charset, jsonModule)
            }
        }

        else -> null
    }?.safeCast()
}

internal fun <K : Any, V : Any> ArrayDeque<ResponseToken>.readMapResponseTyped(
    kClass: KClass<K>,
    vClass: KClass<V>,
    charset: Charset,
    jsonModule: Json? = null,
): Map<K, V>? {
    if (isEmpty()) return null
    val typeToken = validatedResponseType()
    val size = typeToken.size ?: 0

    return when (typeToken.code) {
        RespCode.MAP -> {
            if (size < 0) return null
            buildMap<K, V?>(size) {
                repeat(size) {
                    val keyData = readSimpleResponseTyped(kClass, charset, jsonModule)
                        ?: throw ResponseParsingException(message = "Invalid map key")
                    val valueType = readSimpleResponseTyped(vClass, charset, jsonModule)
                    put(keyData, valueType)
                }
            }
        }

        else -> null
    }?.safeCast()
}
