package eu.vendeli.rethis.utils

import com.ionspin.kotlin.bignum.integer.BigInteger
import eu.vendeli.rethis.exception
import eu.vendeli.rethis.types.core.*
import eu.vendeli.rethis.utils.Const.DEFAULT_REDIS_BUFFER_SIZE
import eu.vendeli.rethis.utils.Const.DEFAULT_REDIS_POOL_CAPACITY
import io.ktor.utils.io.*
import io.ktor.utils.io.pool.*
import kotlinx.io.readByteArray

internal object RedisByteArrayPool : DefaultPool<ByteArray>(DEFAULT_REDIS_POOL_CAPACITY) {
    override fun produceInstance(): ByteArray = ByteArray(DEFAULT_REDIS_BUFFER_SIZE)

    override fun clearInstance(instance: ByteArray): ByteArray = instance.apply { fill(0) }
}

internal suspend fun ByteReadChannel.readRedisMessage(rawOnly: Boolean = false): RType {
    val type = RespCode.fromCode(readByte()) // Read the type byte (e.g., +, -, :, $, *)
    val line = readCRLFLine() // Read until CRLF for simple types

    if (rawOnly) line.toIntOrNull()?.let {
        return RType.Raw(readByteArray(it))
    }

    return when (type) {
        RespCode.SIMPLE_STRING -> PlainString(line) // Return SimpleString type

        RespCode.SIMPLE_ERROR -> RType.Error(line) // Error response

        RespCode.INTEGER -> Int64(line.toLongOrNull() ?: exception { "Invalid number format: $line" })

        RespCode.BULK -> {
            val size = line.toInt() // Parse the size from the bulk string header ($<size>)
            if (size < 0) return RType.Null // Handle null bulk string (`$-1`)
            val content = readRemaining(size.toLong()).readByteArray() // Read the specified size of bytes
            readShort() // Skip CRLF after the bulk string
            BulkString(content.decodeToString())
        }

        RespCode.ARRAY -> {
            val arraySize = line.toInt()
            if (arraySize < 0) return RType.Null // Handle null array (`*-1`)
            val elements = List(arraySize) { readRedisMessage() } // Recursively read each array element
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
            val size = line.toInt() // Parse the size from the bulk error header
            if (size < 0) exception { "Invalid bulk error size: $size" }
            val content = readRemaining(size.toLong()).readByteArray() // Read the error content
            readShort() // Skip CRLF after the bulk error
            RType.Error(content.decodeToString())
        }

        RespCode.VERBATIM_STRING -> {
            val size = line.toInt()
            if (size < 0) return RType.Null // Handle null verbatim string
            val content = readRemaining(size.toLong()).readByteArray()
            readShort() // Skip CRLF
            val encoding = content.decodeToString(0, 3) // First 3 bytes are encoding
            val data = content.decodeToString(4, size - 4) // Skip encoding and colon (:)
            VerbatimString(encoding, data)
        }

        RespCode.MAP -> {
            val mapSize = line.toInt()
            if (mapSize < 0) return RType.Null // Handle null map
            val resultMap = mutableMapOf<RPrimitive, RType>()
            repeat(mapSize) {
                val key = readRedisMessage() as RPrimitive
                val value = readRedisMessage()
                resultMap[key] = value
            }
            RMap(resultMap)
        }

        RespCode.SET -> {
            val setSize = line.toInt()
            if (setSize < 0) return RType.Null // Handle null set
            val resultSet = mutableSetOf<RPrimitive>()
            repeat(setSize) {
                resultSet.add(readRedisMessage() as RPrimitive)
            }
            RSet(resultSet)
        }

        RespCode.PUSH -> {
            val pushSize = line.toInt()
            if (pushSize < 0) return RType.Null // Handle null push message
            val elements = List(pushSize) { readRedisMessage() as RPrimitive }
            Push(elements)
        }
    }
}

internal suspend fun ByteReadChannel.readCRLFLine(): String {
    val result = StringBuilder()
    var lastByte: Byte
    var secondLastByte: Byte = 0
    var bytesRead = 0

    RedisByteArrayPool.useInstance { buffer ->
        while (true) {
            lastByte = readByte()
            if (secondLastByte == '\r'.code.toByte() && lastByte == '\n'.code.toByte()) {
                break // End of line found
            }
            if (bytesRead < buffer.size) {
                buffer[bytesRead] = lastByte // Store byte in buffer
                bytesRead++
            } else {
                // Resize buffer dynamically instead of throwing an exception
                result.append(buffer.decodeToString(0, bytesRead))
                bytesRead = 0
            }
            secondLastByte = lastByte
        }

        // Skip the last CR byte
        bytesRead-- // Exclude the CR

        // Convert the buffer to a string
        result.append(buffer.decodeToString(0, bytesRead))
    }

    return result.toString()
}

internal inline fun <reified L, reified R> RType.unwrapRespIndMap(): Map<L, R?>? =
    if (this is RArray) cast<RArray>().value.chunked(2).associate {
        it.first().unwrap<L>()!! to it.last().unwrap<R>()
    } else unwrapMap<L, R>()
