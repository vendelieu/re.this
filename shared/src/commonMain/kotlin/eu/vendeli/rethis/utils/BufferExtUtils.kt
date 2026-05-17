package eu.vendeli.rethis.utils

import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.TimeUnit
import eu.vendeli.rethis.shared.utils.BYTE_0
import eu.vendeli.rethis.shared.utils.BYTE_MINUS
import eu.vendeli.rethis.shared.utils.CARRIAGE_RETURN_BYTE
import eu.vendeli.rethis.shared.utils.NEWLINE_BYTE
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.DelicateIoApi
import kotlinx.io.UnsafeIoApi
import kotlinx.io.unsafe.UnsafeBufferOperations
import kotlinx.io.writeDecimalLong
import kotlinx.io.writeString
import kotlinx.io.writeToInternalBuffer
import kotlin.time.Duration
import kotlin.time.Instant

@OptIn(DelicateIoApi::class, UnsafeIoApi::class)
private fun Buffer.appendEOL() {
    writeToInternalBuffer { buffer ->
        UnsafeBufferOperations.writeToTail(buffer, 2) { ctx, segment ->
            ctx.setUnchecked(segment, 0, CARRIAGE_RETURN_BYTE)
            ctx.setUnchecked(segment, 1, NEWLINE_BYTE)
            2
        }
    }
}

private fun Buffer.append(type: RespCode) {
    writeByte(type.code)
}

private fun Buffer.writeBA(value: ByteArray) {
    append(RespCode.BULK)
    writeDecimalLong(value.size.toLong())
    appendEOL()
    writeFully(value)
    appendEOL()
}

private val LONG_POW10 = longArrayOf(
    1L, 10L, 100L, 1_000L, 10_000L, 100_000L, 1_000_000L, 10_000_000L,
    100_000_000L, 1_000_000_000L, 10_000_000_000L, 100_000_000_000L,
    1_000_000_000_000L, 10_000_000_000_000L, 100_000_000_000_000L,
    1_000_000_000_000_000L, 10_000_000_000_000_000L, 100_000_000_000_000_000L,
    1_000_000_000_000_000_000L,
)

// Hacker's Delight 11-4 / JDK Long.stringSize: digit count from leading zero bits.
private fun decimalDigits(value: Long): Int {
    val abs = when {
        value == Long.MIN_VALUE -> Long.MAX_VALUE
        value < 0 -> -value
        else -> value
    }
    val approx = ((64 - abs.countLeadingZeroBits()) * 1233) ushr 12
    val needsBump = approx >= LONG_POW10.size || abs >= LONG_POW10[approx]
    return (if (needsBump) approx + 1 else approx).coerceAtLeast(1)
}

private fun Buffer.writeDecimalAsBulk(value: Long) {
    val byteLen = decimalDigits(value) + if (value < 0) 1 else 0
    append(RespCode.BULK)
    writeDecimalLong(byteLen.toLong())
    appendEOL()
    writeDecimalLong(value)
    appendEOL()
}

fun Buffer.writeStringArg(value: String, charset: Charset) = writeBA(value.toByteArray(charset))
fun Buffer.writeLongArg(value: Long, charset: Charset) = writeDecimalAsBulk(value)
fun Buffer.writeIntArg(value: Int, charset: Charset) = writeDecimalAsBulk(value.toLong())
fun Buffer.writeDoubleArg(value: Double, charset: Charset) = writeBA(value.toString().toByteArray(charset))
fun Buffer.writeByteArrayArg(value: ByteArray, charset: Charset) = writeBA(value)

fun Buffer.writeBooleanArg(value: Boolean, charset: Charset) =
    writeBA(value.let { if (it) "t" else "f" }.toByteArray(charset))

fun Buffer.writeArrayHeader(count: Int) {
    append(RespCode.ARRAY)
    writeDecimalLong(count.toLong())
    appendEOL()
}

fun Buffer.writeBulkString(data: ByteArray) {
    writeBA(data)
}
