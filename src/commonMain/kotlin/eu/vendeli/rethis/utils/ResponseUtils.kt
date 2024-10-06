package eu.vendeli.rethis.utils

import com.ionspin.kotlin.bignum.integer.BigInteger
import eu.vendeli.rethis.exception
import eu.vendeli.rethis.types.core.*
import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer

internal suspend fun ByteReadChannel.readRedisMessage(charset: Charset, rawOnly: Boolean = false): RType {
    val type = RespCode.fromCode(readByte()) // Read the type byte (e.g., +, -, :, $, *)
    val line = readCRLFLine(charset)

    if (rawOnly) line.toIntOrNull()?.let {
        return RType.Raw(readByteArray(it))
    }

    return when (type) {
        RespCode.SIMPLE_STRING -> PlainString(line) // Return SimpleString type

        RespCode.SIMPLE_ERROR -> RType.Error(line) // Error response

        RespCode.INTEGER -> Int64(line.toLongOrNull() ?: exception { "Invalid number format: $line" })

        RespCode.BULK -> {
            val size = line.toLong() // Parse the size from the bulk string header ($<size>)
            if (size < 0) return RType.Null // Handle null bulk string (`$-1`)
            val content = readRemaining(size).readText(charset) // Read the specified size of bytes
            readShort() // Skip CRLF after the bulk string
            BulkString(content)
        }

        RespCode.ARRAY -> {
            val arraySize = line.toInt()
            if (arraySize < 0) return RType.Null // Handle null array (`*-1`)
            val elements = List(arraySize) { readRedisMessage(charset, rawOnly) } // Recursively read each array element
            RArray(elements)
        }

        RespCode.NULL -> RType.Null

        RespCode.BOOLEAN -> when (line) {
            "t" -> Bool(true)
            "f" -> Bool(false)
            else -> exception { "Invalid boolean format: $line" }
        }

        RespCode.DOUBLE -> F64(line.toDoubleOrNull() ?: exception { "Invalid double format: $line" })

        RespCode.BIG_NUMBER -> try {
            BigNumber(BigInteger.parseString(line))
        } catch (e: NumberFormatException) {
            exception(e) { "Invalid BigInteger format: $line" }
        }

        RespCode.BULK_ERROR -> {
            val size = line.toLong() // Parse the size from the bulk error header
            if (size < 0) exception { "Invalid bulk error size: $size" }
            val content = readRemaining(size) // Read the error content
            readShort() // Skip CRLF after the bulk error
            RType.Error(content.readText(charset))
        }

        RespCode.VERBATIM_STRING -> {
            val size = line.toLong()
            if (size < 0) return RType.Null // Handle null verbatim string
            val content = readRemaining(size).readText(charset)
            readShort() // Skip CRLF
            val encoding = content.subSequence(0, 3) // First 3 bytes are encoding
            val data = content.subSequence(4, size.toInt() - 4) // Skip encoding and colon (:)
            VerbatimString(encoding.toString(), data.toString())
        }

        RespCode.MAP -> {
            val mapSize = line.toInt()
            if (mapSize < 0) return RType.Null // Handle null map
            val resultMap = mutableMapOf<RPrimitive, RType>()
            repeat(mapSize) {
                val key = readRedisMessage(charset, rawOnly) as RPrimitive
                val value = readRedisMessage(charset, rawOnly)
                resultMap[key] = value
            }
            RMap(resultMap)
        }

        RespCode.SET -> {
            val setSize = line.toInt()
            if (setSize < 0) return RType.Null // Handle null set
            val resultSet = mutableSetOf<RPrimitive>()
            repeat(setSize) {
                resultSet.add(readRedisMessage(charset, rawOnly) as RPrimitive)
            }
            RSet(resultSet)
        }

        RespCode.PUSH -> {
            val pushSize = line.toInt()
            if (pushSize < 0) return RType.Null // Handle null push message
            val elements = List(pushSize) { readRedisMessage(charset, rawOnly) as RPrimitive }
            Push(elements)
        }
    }
}


internal suspend fun ByteReadChannel.readCRLFLine(charset: Charset): String {
    val buffer = Buffer()
    while (true) {
        val byte = readByte()
        buffer.writeByte(byte)
        if (byte == '\n'.code.toByte() && buffer.size > 1 && buffer[buffer.size - 2] == '\r'.code.toByte()) {
            break // End of line found
        }
    }
    return buffer.readText(charset, (buffer.size - 2).toInt())
}

internal inline fun <reified L, reified R> RType.unwrapRespIndMap(): Map<L, R?>? =
    if (this is RArray) cast<RArray>().value.chunked(2).associate {
        it.first().unwrap<L>()!! to it.last().unwrap<R>()
    } else unwrapMap<L, R>()
