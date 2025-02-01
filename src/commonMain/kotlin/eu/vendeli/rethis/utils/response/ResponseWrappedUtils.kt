package eu.vendeli.rethis.utils.response

import com.ionspin.kotlin.bignum.integer.BigInteger
import eu.vendeli.rethis.ResponseParsingException
import eu.vendeli.rethis.types.core.*
import eu.vendeli.rethis.types.core.RType.Error
import eu.vendeli.rethis.types.core.ResponseToken.Code
import eu.vendeli.rethis.utils.Const.FALSE_BYTE
import eu.vendeli.rethis.utils.Const.TRUE_BYTE
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.readByteArray
import kotlinx.io.readDecimalLong
import kotlinx.io.readString

internal fun ArrayDeque<ResponseToken>.readResponseWrapped(
    charset: Charset,
    rawOnly: Boolean = false,
): RType {
    if (isEmpty()) return RType.Null
    val typeToken = validatedResponseType()
    val size = typeToken.size ?: 0

    return when (typeToken.code) {
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
    code: Code? = null,
): RType {
    val typeToken = code ?: validatedResponseType()
    val size = typeToken.size ?: 0
    if (size == -1) return RType.Null
    val data = validatedSimpleResponse(typeToken)
    if (rawOnly) return RType.Raw(data.readByteArray(size))

    return when (typeToken.code) {
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
            else -> throw ResponseParsingException("Invalid boolean format")
        }

        RespCode.DOUBLE -> F64(data.readString().toDouble())

        RespCode.BIG_NUMBER -> try {
            BigNumber(BigInteger.parseString(data.readText(charset)))
        } catch (e: NumberFormatException) {
            throw ResponseParsingException("Invalid BigInteger format", e)
        }

        RespCode.BULK_ERROR -> {
            if (size < 0) throw ResponseParsingException("Invalid bulk error size: $size")
            Error(data.readText(charset))
        }

        RespCode.VERBATIM_STRING -> {
            if (size < 0) return RType.Null
            val content = data.readText(charset)
            val encoding = content.subSequence(0, 3)
            val data = content.subSequence(4, size.toInt())
            VerbatimString(encoding.toString(), data.toString())
        }

        RespCode.ARRAY, RespCode.SET, RespCode.PUSH, RespCode.MAP, RespCode.ATTRIBUTE ->
            throw ResponseParsingException(message = "Invalid response type for simple response: ${typeToken.code}")
    }
}
