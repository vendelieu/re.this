package eu.vendeli.rethis.utils

import com.ionspin.kotlin.bignum.integer.BigInteger
import eu.vendeli.rethis.ReThisException
import eu.vendeli.rethis.exception
import eu.vendeli.rethis.types.core.*
import eu.vendeli.rethis.utils.Const.CARRIAGE_RETURN_BYTE
import eu.vendeli.rethis.utils.Const.FALSE_BYTE
import eu.vendeli.rethis.utils.Const.NEWLINE_BYTE
import eu.vendeli.rethis.utils.Const.TRUE_BYTE
import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.readDecimalLong
import kotlinx.io.readString

internal suspend fun ByteReadChannel.readRedisMessage(charset: Charset, rawOnly: Boolean = false): RType {
    val type = RespCode.fromCode(readByte()) // Read the type byte (e.g., +, -, :, $, *)
    val line = readLine2Buffer()

    if (rawOnly) return RType.Raw(readByteArray(line.readDecimalLong().toInt()))

    return when (type) {
        RespCode.SIMPLE_STRING -> PlainString(line.readText(charset)) // Return SimpleString type

        RespCode.SIMPLE_ERROR -> RType.Error(line.readText(charset)) // Error response

        RespCode.INTEGER -> Int64(line.readDecimalLong())

        RespCode.BULK -> {
            val size = line.readDecimalLong() // Parse the size from the bulk string header ($<size>)
            if (size < 0) return RType.Null // Handle null bulk string (`$-1`)
            val content = readRemaining(size.toLong()).readText(charset) // Read the specified size of bytes
            readShort() // Skip CRLF after the bulk string
            BulkString(content)
        }

        RespCode.ARRAY -> {
            val arraySize = line.readDecimalLong()
            if (arraySize < 0) return RType.Null // Handle null array (`*-1`)
            val elements =
                List(arraySize.toInt()) { readRedisMessage(charset, rawOnly) } // Recursively read each array element
            RArray(elements)
        }

        RespCode.NULL -> RType.Null

        RespCode.BOOLEAN -> when (line.readByte()) {
            TRUE_BYTE -> Bool(true)
            FALSE_BYTE -> Bool(false)
            else -> exception { "Invalid boolean format: $line" }
        }

        RespCode.DOUBLE -> F64(line.readString().toDouble())

        RespCode.BIG_NUMBER -> try {
            BigNumber(BigInteger.parseString(line.readText(charset)))
        } catch (e: NumberFormatException) {
            exception(e) { "Invalid BigInteger format: $line" }
        }

        RespCode.BULK_ERROR -> {
            val size = line.readDecimalLong() // Parse the size from the bulk error header
            if (size < 0) exception { "Invalid bulk error size: $size" }
            val content = readRemaining(size) // Read the error content
            readShort() // Skip CRLF after the bulk error
            RType.Error(content.readText(charset))
        }

        RespCode.VERBATIM_STRING -> {
            val size = line.readDecimalLong()
            if (size < 0) return RType.Null // Handle null verbatim string
            val content = readRemaining(size).readText(charset)
            readShort() // Skip CRLF
            val encoding = content.subSequence(0, 3) // First 3 bytes are encoding
            val data = content.subSequence(4, size.toInt() - 4) // Skip encoding and colon (:)
            VerbatimString(encoding.toString(), data.toString())
        }

        RespCode.MAP -> {
            val mapSize = line.readDecimalLong()
            if (mapSize < 0) return RType.Null // Handle null map
            val resultMap = mutableMapOf<RPrimitive, RType>()
            (1..mapSize).forEach {
                val key = readRedisMessage(charset, rawOnly) as RPrimitive
                val value = readRedisMessage(charset, rawOnly)
                resultMap[key] = value
            }
            RMap(resultMap)
        }

        RespCode.SET -> {
            val setSize = line.readDecimalLong()
            if (setSize < 0) return RType.Null // Handle null set
            val resultSet = mutableSetOf<RPrimitive>()
            (1..setSize).forEach {
                resultSet.add(readRedisMessage(charset, rawOnly) as RPrimitive)
            }
            RSet(resultSet)
        }

        RespCode.PUSH -> {
            val pushSize = line.readDecimalLong()
            if (pushSize < 0) return RType.Null // Handle null push message
            val elements = List(pushSize.toInt()) { readRedisMessage(charset, rawOnly) as RPrimitive }
            Push(elements)
        }
    }
}

internal suspend inline fun <T> ByteReadChannel.processRedisSimpleResponse(
    charset: Charset,
): T? {
    val type = RespCode.fromCode(readByte()) // Read the type byte (e.g., +, -, :, $, *)
    val line = readLine2Buffer()

    return when (type) {
        RespCode.SIMPLE_STRING -> line.readText(charset) // Return SimpleString type
        RespCode.SIMPLE_ERROR -> throw ReThisException(line.readText(charset)) // Error response
        RespCode.INTEGER -> line.readDecimalLong()
        RespCode.BULK -> {
            val size = line.readDecimalLong() // Parse the size from the bulk string header ($<size>)
            if (size < 0) return null // Handle null bulk string (`$-1`)
            val content = readRemaining(size.toLong()).readText(charset) // Read the specified size of bytes
            readShort() // Skip CRLF after the bulk string
            content
        }

        RespCode.NULL -> null
        RespCode.BOOLEAN -> when (line.readByte()) {
            TRUE_BYTE -> true
            FALSE_BYTE -> false
            else -> exception { "Invalid boolean format: $line" }
        }

        RespCode.DOUBLE -> line.readString().toDouble()

        RespCode.BIG_NUMBER -> try {
            BigInteger.parseString(line.readText(charset))
        } catch (e: NumberFormatException) {
            exception(e) { "Invalid BigInteger format: $line" }
        }

        RespCode.BULK_ERROR -> {
            val size = line.readDecimalLong() // Parse the size from the bulk error header
            if (size < 0) exception { "Invalid bulk error size: $size" }
            val content = readRemaining(size) // Read the error content
            readShort() // Skip CRLF after the bulk error
            throw ReThisException(content.readText(charset))
        }

        RespCode.VERBATIM_STRING -> {
            val size = line.readDecimalLong()
            if (size < 0) return null // Handle null verbatim string
            val content = readRemaining(size).readText(charset)
            readShort() // Skip CRLF
            val encoding = content.subSequence(0, 3) // First 3 bytes are encoding
            val data = content.subSequence(4, size.toInt() - 4) // Skip encoding and colon (:)
            "$encoding:$data"
        }

        else -> null
    }?.safeCast()
}

internal suspend inline fun <T> ByteReadChannel.processRedisListResponse(
    charset: Charset,
): List<T>? {
    val type = RespCode.fromCode(readByte()) // Read the type byte (e.g., +, -, :, $, *)
    val line = readLine2Buffer()

    return when (type) {
        RespCode.ARRAY -> {
            val arraySize = line.readDecimalLong()
            if (arraySize < 0) return null // Handle null array (`*-1`)
            List(arraySize.toInt()) {
                processRedisSimpleResponse<T>(charset)
            } // Recursively read each array element
        }

        RespCode.SET -> {
            val setSize = line.readDecimalLong()
            if (setSize < 0) return null // Handle null set
            List(setSize.toInt()) {
                processRedisSimpleResponse<T>(charset)
            }
        }

        RespCode.PUSH -> {
            val pushSize = line.readDecimalLong()
            if (pushSize < 0) return null // Handle null push message
            List(pushSize.toInt()) {
                processRedisSimpleResponse<T>(charset)
            }
        }

        else -> null
    }?.safeCast()
}

internal suspend fun <K : Any, V : Any> ByteReadChannel.processRedisMapResponse(
    charset: Charset,
): Map<K, V>? {
    val type = RespCode.fromCode(readByte()) // Read the type byte (e.g., +, -, :, $, *)
    val line = readLine2Buffer()

    return when (type) {
        RespCode.MAP -> {
            val mapSize = line.readDecimalLong()
            if (mapSize < 0) return null // Handle null map
            buildMap<K, V?>(mapSize.toInt()) {
                (1..mapSize.toInt()).forEach {
                    val key = processRedisSimpleResponse<K>(charset) ?: exception { "Invalid map key" }
                    val value = processRedisSimpleResponse<V>(charset)
                    put(key, value)
                }
            }
        }

        else -> null
    }?.safeCast()
}

/**
 * Reads a line from the `ByteReadChannel` into a `Buffer`, stopping at a CRLF sequence.
 *
 * The method reads bytes one by one and appends them to a buffer until it encounters
 * a carriage return followed by a newline (CRLF). The CRLF sequence is not included
 * in the returned buffer.
 *
 * @return A `Buffer` containing the line read from the channel, excluding the CRLF.
 */
private suspend fun ByteReadChannel.readLine2Buffer(): Buffer {
    val buffer = Buffer()
    while (true) {
        val byte = readByte()

        if (byte == CARRIAGE_RETURN_BYTE) {
            val nextByte = readByte()
            if (nextByte == NEWLINE_BYTE) {
                break // End of line found
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
