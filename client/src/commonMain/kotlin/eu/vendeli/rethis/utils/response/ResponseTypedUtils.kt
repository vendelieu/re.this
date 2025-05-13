package eu.vendeli.rethis.utils.response

import com.ionspin.kotlin.bignum.integer.BigInteger
import eu.vendeli.rethis.api.spec.common.types.RedisError
import eu.vendeli.rethis.api.spec.common.types.ResponseParsingException
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.types.common.ResponseToken
import eu.vendeli.rethis.utils.Const.FALSE_BYTE
import eu.vendeli.rethis.utils.Const.TRUE_BYTE
import eu.vendeli.rethis.utils.safeCast
import io.ktor.util.reflect.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlinx.io.readDecimalLong
import kotlinx.io.readString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.serializer

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalSerializationApi::class)
private inline fun Source.decode(
    typeInfo: TypeInfo,
    charset: Charset,
    jsonModule: Json? = null,
): Any? = when {
    typeInfo.type == String::class -> readText(charset)
    jsonModule != null -> jsonModule.decodeFromSource(
        jsonModule.serializersModule.serializer(typeInfo.kotlinType!!),
        this,
    )

    else -> readText(charset)
}

@Throws(RedisError::class, ResponseParsingException::class)
internal fun <T : Any> ArrayDeque<ResponseToken>.readSimpleResponseTyped(
    typeInfo: TypeInfo,
    charset: Charset,
    jsonModule: Json? = null,
): T? {
    if (isEmpty()) return null
    val typeToken = validatedResponseType()
    val size = typeToken.size ?: 0
    if (size == -1) return null
    val data = validatedSimpleResponse(typeToken)

    return when (typeToken.code) {
        RespCode.SIMPLE_STRING -> data.decode(typeInfo, charset, jsonModule)
        RespCode.SIMPLE_ERROR -> throw RedisError(data.readText(charset))
        RespCode.INTEGER -> data.readDecimalLong()
        RespCode.BULK -> {
            if (size < 0) return null
            data.decode(typeInfo, charset, jsonModule)
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
            data.readByte() // skip : byte
            if (jsonModule == null) {
                val encoding = encodingBytes.decodeToString()
                val content = data.readText(charset)

                "$encoding:$content"
            } else {
                data.decode(typeInfo, charset, jsonModule)
            }
        }

        else -> null
    }?.safeCast(typeInfo)
}

internal fun <T : Any> ArrayDeque<ResponseToken>.readListResponseTyped(
    type: TypeInfo,
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
                readSimpleResponseTyped<T>(type, charset, jsonModule)
            }
        }

        else -> null
    }?.safeCast()
}

internal fun <K : Any, V : Any> ArrayDeque<ResponseToken>.readMapResponseTyped(
    kType: TypeInfo,
    vType: TypeInfo,
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
                    val keyData = readSimpleResponseTyped<K>(kType, charset, jsonModule)
                        ?: throw ResponseParsingException(message = "Invalid map key")
                    val valueType = readSimpleResponseTyped<V>(vType, charset, jsonModule)
                    put(keyData, valueType)
                }
            }
        }

        else -> null
    }?.safeCast()
}
