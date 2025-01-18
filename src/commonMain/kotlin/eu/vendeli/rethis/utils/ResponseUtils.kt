package eu.vendeli.rethis.utils

import com.ionspin.kotlin.bignum.integer.BigInteger
import eu.vendeli.rethis.ReThisException
import eu.vendeli.rethis.exception
import eu.vendeli.rethis.types.core.*
import eu.vendeli.rethis.types.core.RType.Error
import eu.vendeli.rethis.types.core.ResponseToken.Data
import eu.vendeli.rethis.types.core.ResponseToken.Type
import eu.vendeli.rethis.utils.Const.CARRIAGE_RETURN_BYTE
import eu.vendeli.rethis.utils.Const.FALSE_BYTE
import eu.vendeli.rethis.utils.Const.NEWLINE_BYTE
import eu.vendeli.rethis.utils.Const.TRUE_BYTE
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlinx.io.readDecimalLong
import kotlinx.io.readString
import kotlin.reflect.KClass

// Common utils

internal suspend fun ByteReadChannel.parseResponse(): ArrayDeque<ResponseToken> {
    val response = ArrayDeque<ResponseToken>()
    if (availableForRead <= 0) awaitContent()
    while (availableForRead > 0) {
        val line = readLineCRLF()
        val type = RespCode.fromCode(line.readByte())
        when (type) {
            RespCode.BULK, RespCode.BULK_ERROR, RespCode.VERBATIM_STRING -> {
                val size = line.readDecimalLong()
                response.addLast(Type(type, size.toInt()))

                if (size > 0) {
                    response.addLast(Data(readRemaining(size)))
                    readShort() // skip CRLF
                }
            }

            RespCode.ARRAY, RespCode.SET, RespCode.PUSH, RespCode.MAP -> {
                val size = line.readDecimalLong()
                response.addLast(Type(type, size.toInt()))
            }

            else -> {
                response.addLast(Type(type))
                response.addLast(Data(line))
            }
        }
    }
    return response
}

internal suspend inline fun Connection.parseResponse(): ArrayDeque<ResponseToken> = input.parseResponse()
internal suspend inline fun Connection.readResponseWrapped(
    charset: Charset,
    rawOnly: Boolean = false,
) = parseResponse().readResponseWrapped(charset, rawOnly)

private suspend inline fun ByteReadChannel.readLineCRLF(): kotlinx.io.Buffer {
    val buffer = kotlinx.io.Buffer()
    while (true) {
        val byte = readByte()

        if (byte == CARRIAGE_RETURN_BYTE) {
            val nextByte = readByte()
            if (nextByte == NEWLINE_BYTE) {
                break
            } else {
                buffer.writeByte(CARRIAGE_RETURN_BYTE)
                buffer.writeByte(NEWLINE_BYTE)
                continue
            }
        }
        buffer.writeByte(byte)
    }
    return buffer
}

internal inline fun <reified L, reified R> RType.unwrapRespIndMap(): Map<L, R?>? =
    if (this is RArray) cast<RArray>().value.chunked(2).associate {
        it.first().unwrap<L>()!! to it.last().unwrap<R>()
    } else unwrapMap<L, R>()

@Suppress("NOTHING_TO_INLINE")
private inline fun ArrayDeque<ResponseToken>.validatedResponseType(): Type {
    val typeToken = removeFirst()
    if (typeToken !is Type) exception {
        "Invalid response structure, wrong head token, expected type token but given $typeToken"
    }
    return typeToken
}

@Suppress("NOTHING_TO_INLINE")
private inline fun ArrayDeque<ResponseToken>.validatedSimpleResponse(typeToken: Type): Source {
    if (!RespCode.isSimpleType(typeToken.type)) exception {
        "Wrong response type, expected simple type, given ${typeToken.type}"
    }

    if (typeToken.type != RespCode.NULL && isEmpty()) exception {
        "Invalid response structure, expected data token, given $typeToken"
    }
    val dataToken = removeFirst()

    if (dataToken !is Data) exception {
        "Invalid response structure, expected data token, given $dataToken"
    }

    return dataToken.buffer
}

// Wrapped response parsing

internal fun ArrayDeque<ResponseToken>.readResponseWrapped(
    charset: Charset,
    rawOnly: Boolean = false,
): RType {
    if (isEmpty()) return RType.Null
    val typeToken = validatedResponseType()
    val size = typeToken.size ?: 0

    return when (typeToken.type) {
        RespCode.ARRAY -> {
            if (size < 0) return RType.Null
            val elements = List(size) {
                readResponseWrapped(charset, rawOnly)
            }
            RArray(elements)
        }

        RespCode.SET -> {
            if (size < 0) return RType.Null
            val resultSet = mutableSetOf<RPrimitive>()
            repeat(size) {
                resultSet.add(readResponseWrapped(charset, rawOnly) as RPrimitive)
            }
            RSet(resultSet)
        }

        RespCode.PUSH -> {
            if (size < 0) return RType.Null
            val elements = List(size) {
                readResponseWrapped(charset, rawOnly) as RPrimitive
            }
            Push(elements)
        }

        RespCode.MAP -> {
            if (size < 0) return RType.Null
            val resultMap = mutableMapOf<RPrimitive, RType>()
            repeat(size) {
                val keyData = readResponseWrapped(charset, rawOnly)
                val valueType = readResponseWrapped(charset, rawOnly)

                resultMap[keyData as RPrimitive] = valueType
            }
            RMap(resultMap)
        }

        else -> readSimpleResponseWrapped(charset, rawOnly, typeToken)
    }
}

private fun ArrayDeque<ResponseToken>.readSimpleResponseWrapped(
    charset: Charset,
    rawOnly: Boolean = false,
    type: Type? = null,
): RType {
    val typeToken = type ?: validatedResponseType()
    val size = typeToken.size ?: 0
    if (size == -1) return RType.Null
    val data = validatedSimpleResponse(typeToken)
    if (rawOnly) return RType.Raw(data.readByteArray(size))

    return when (typeToken.type) {
        RespCode.SIMPLE_STRING -> PlainString(data.readText(charset))

        RespCode.SIMPLE_ERROR -> Error(data.readText(charset))

        RespCode.INTEGER -> Int64(data.readDecimalLong())

        RespCode.NULL -> RType.Null

        RespCode.BULK -> {
            if (size < 0) return RType.Null
            val content = data.readText(charset)
            BulkString(content)
        }

        RespCode.BOOLEAN -> when (data.readByte()) {
            TRUE_BYTE -> Bool(true)
            FALSE_BYTE -> Bool(false)
            else -> exception { "Invalid boolean format" }
        }

        RespCode.DOUBLE -> F64(data.readString().toDouble())

        RespCode.BIG_NUMBER -> try {
            BigNumber(BigInteger.parseString(data.readText(charset)))
        } catch (e: NumberFormatException) {
            exception(e) { "Invalid BigInteger format" }
        }

        RespCode.BULK_ERROR -> {
            if (size < 0) exception { "Invalid bulk error size: $size" }
            Error(data.readText(charset))
        }

        RespCode.VERBATIM_STRING -> {
            if (size < 0) return RType.Null
            val content = data.readText(charset)
            val encoding = content.subSequence(0, 3)
            val data = content.subSequence(4, size.toInt())
            VerbatimString(encoding.toString(), data.toString())
        }

        RespCode.ARRAY, RespCode.MAP, RespCode.SET, RespCode.PUSH -> exception {
            "Invalid response type for simple response: ${typeToken.type}"
        }
    }
}

// Typed response parsing

internal fun <T : Any> ArrayDeque<ResponseToken>.readSimpleResponseTyped(
    tClass: KClass<T>,
    charset: Charset,
): T? {
    if (isEmpty()) return null
    val typeToken = validatedResponseType()
    val size = typeToken.size ?: 0
    if (size == -1) return null
    val data = validatedSimpleResponse(typeToken)

    return when (typeToken.type) {
        RespCode.SIMPLE_STRING -> data.readText(charset)
        RespCode.SIMPLE_ERROR -> throw ReThisException(data.readText(charset))
        RespCode.INTEGER -> data.readDecimalLong()
        RespCode.BULK -> {
            if (size < 0) return null
            data.readText(charset)
        }

        RespCode.BOOLEAN -> when (val line = data.readByte()) {
            TRUE_BYTE -> true
            FALSE_BYTE -> false
            else -> exception { "Invalid boolean format: $line" }
        }

        RespCode.DOUBLE -> data.readString().toDouble()

        RespCode.BIG_NUMBER -> try {
            BigInteger.parseString(data.readText(charset))
        } catch (e: NumberFormatException) {
            exception(e) { "Invalid BigInteger format" }
        }

        RespCode.BULK_ERROR -> {
            if (size < 0) exception { "Invalid bulk error size: $size" }
            throw ReThisException(data.readText(charset))
        }

        RespCode.VERBATIM_STRING -> {
            if (size < 0) return null
            val content = data.readText(charset)
            val encoding = content.subSequence(0, 3)
            val data = content.subSequence(4, size.toInt())
            "$encoding:$data"
        }

        else -> null
    }?.safeCast(tClass)
}

internal fun <T : Any> ArrayDeque<ResponseToken>.readListResponseTyped(
    tClass: KClass<T>,
    charset: Charset,
): List<T>? {
    if (isEmpty()) return null
    val typeToken = validatedResponseType()
    val size = typeToken.size ?: 0

    return when (typeToken.type) {
        RespCode.ARRAY, RespCode.SET, RespCode.PUSH -> {
            if (size < 0) return null
            List(size) {
                readSimpleResponseTyped<T>(tClass, charset)
            }
        }

        else -> null
    }?.safeCast()
}

internal fun <K : Any, V : Any> ArrayDeque<ResponseToken>.readMapResponseTyped(
    kClass: KClass<K>,
    vClass: KClass<V>,
    charset: Charset,
): Map<K, V>? {
    if (isEmpty()) return null
    val typeToken = validatedResponseType()
    val size = typeToken.size ?: 0

    return when (typeToken.type) {
        RespCode.MAP -> {
            if (size < 0) return null
            buildMap<K, V?>(size) {
                repeat(size) {
                    val keyData = readSimpleResponseTyped(kClass, charset) ?: exception { "Invalid map key" }
                    val valueType = readSimpleResponseTyped(vClass, charset)
                    put(keyData, valueType)
                }
            }
        }

        else -> null
    }?.safeCast()
}
