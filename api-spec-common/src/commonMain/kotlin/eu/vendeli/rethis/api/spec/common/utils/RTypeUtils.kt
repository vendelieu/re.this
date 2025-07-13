package eu.vendeli.rethis.api.spec.common.utils

import com.ionspin.kotlin.bignum.integer.BigInteger
import eu.vendeli.rethis.api.spec.common.types.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.io.readDecimalLong
import kotlinx.io.readLineStrict

fun Buffer.readResponseWrapped(
    charset: Charset,
    rawOnly: Boolean = false,
): RType {
    // Peek first byte for type
    if (remaining == 0L) return RType.Null
    val prefix = readByte()
    val code = RespCode.fromCode(prefix)
    val size = readLineStrict().toInt()

    return when (code) {
        RespCode.ARRAY -> {
            if (size < 0) RType.Null else {
                val list = mutableListOf<RType>()
                repeat(size) { list += readResponseWrapped(charset, rawOnly) }
                RArray(list)
            }
        }

        RespCode.SET -> {
            if (size < 0) RType.Null else {
                val set = mutableSetOf<RPrimitive>()
                repeat(size) { set += readResponseWrapped(charset, rawOnly) as RPrimitive }
                RSet(set)
            }
        }

        RespCode.PUSH -> {
            if (size < 0) RType.Null else {
                val list = mutableListOf<RPrimitive>()
                repeat(size) { list += readResponseWrapped(charset, rawOnly) as RPrimitive }
                Push(list)
            }
        }

        RespCode.MAP -> {
            if (size < 0) RType.Null else {
                val map = mutableMapOf<RPrimitive, RType>()
                repeat(size) {
                    val key = readResponseWrapped(charset, rawOnly) as RPrimitive
                    val value = readResponseWrapped(charset, rawOnly)
                    map[key] = value
                }
                RMap(map)
            }
        }

        else -> readSimpleResponseWrapped(charset, rawOnly, prefix)
    }
}

private val TRUE_BYTE = 't'.code.toByte()
private val FALSE_BYTE = 'f'.code.toByte()

private fun Buffer.readSimpleResponseWrapped(
    charset: Charset,
    rawOnly: Boolean,
    prefix: Byte? = null,
): RType {
    val code = RespCode.fromCode(prefix ?: readByte())
    if (size == 0L) return RType.Null
    if (rawOnly) return RType.Raw(readByteArray())

    return when (code) {
        RespCode.SIMPLE_STRING -> PlainString(readLineCRLF().readText(charset))
        RespCode.SIMPLE_ERROR -> RType.Error(readLineCRLF().readText(charset))
        RespCode.INTEGER -> Int64(readLineCRLF().readDecimalLong())
        RespCode.BOOLEAN -> when (readByte()) {
            TRUE_BYTE -> Bool(true)
            FALSE_BYTE -> Bool(false)
            else -> throw ResponseParsingException("Invalid boolean format")
        }.also { readLineCRLF() }

        RespCode.DOUBLE -> F64(readLineStrict().toDouble())
        RespCode.BIG_NUMBER -> try {
            BigNumber(BigInteger.parseString(readLineCRLF().readText(charset)))
        } catch (e: NumberFormatException) {
            throw ResponseParsingException("Invalid BigInteger format", e)
        }

        RespCode.BULK -> {
            val size = readLineStrict().toInt()
            if (size < 0) return RType.Null
            val content = readText(charset)
            BulkString(content)
        }

        RespCode.BULK_ERROR -> RType.Error(readLineCRLF().readText(charset))
        RespCode.VERBATIM_STRING -> {
            val size = readLineStrict().toInt()
            if (size < 0) return RType.Null
            val encoding = readByteArray(3).decodeToString()
            readByte() // skip ':' byte
            val content = readLineCRLF().readText(charset)
            VerbatimString(encoding, content)
        }

        RespCode.NULL -> RType.Null
        else -> throw ResponseParsingException("Unexpected simple code: $code")
    }
}


private val NEWLINE_BYTE = '\n'.code.toByte()
private val CARRIAGE_RETURN_BYTE = '\r'.code.toByte()

private inline fun Buffer.readLineCRLF(): Buffer {
    val buffer = Buffer()
    while (true) {
        val byte = readByte()

        if (byte == CARRIAGE_RETURN_BYTE) {
            val nextByte = readByte()
            if (nextByte == NEWLINE_BYTE) {
                break
            } else {
                buffer.writeByte(CARRIAGE_RETURN_BYTE)
                buffer.writeByte(nextByte)
                continue
            }
        }
        buffer.writeByte(byte)
    }
    return buffer
}
