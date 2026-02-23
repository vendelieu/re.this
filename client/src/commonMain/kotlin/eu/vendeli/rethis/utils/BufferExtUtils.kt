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

fun Buffer.writeStringArg(value: String, charset: Charset) = writeBA(value.toByteArray(charset))
fun Buffer.writeLongArg(value: Long, charset: Charset) = writeBA(value.toString().toByteArray(charset))
fun Buffer.writeIntArg(value: Int, charset: Charset) = writeBA(value.toString().toByteArray(charset))
fun Buffer.writeDoubleArg(value: Double, charset: Charset) = writeBA(value.toString().toByteArray(charset))
fun Buffer.writeByteArrayArg(value: ByteArray, charset: Charset) = writeBA(value)

fun Buffer.writeDurationArg(value: Duration, charset: Charset, timeUnit: TimeUnit) = when (timeUnit) {
    TimeUnit.MILLISECONDS -> writeBA(value.inWholeMilliseconds.toString().toByteArray(charset))
    TimeUnit.SECONDS -> writeBA(value.inWholeSeconds.toString().toByteArray(charset))
}

fun Buffer.writeInstantArg(value: Instant, charset: Charset, timeUnit: TimeUnit) = when (timeUnit) {
    TimeUnit.MILLISECONDS -> writeBA(value.toEpochMilliseconds().toString().toByteArray(charset))
    TimeUnit.SECONDS -> writeBA(value.epochSeconds.toString().toByteArray(charset))
}

fun Buffer.writeBooleanArg(value: Boolean, charset: Charset) =
    writeBA(value.let { if (it) "t" else "f" }.toByteArray(charset))

fun Buffer.writeArrayHeader(count: Int) {
    append(RespCode.ARRAY)
    writeDecimalLong(count.toLong())
}

fun Buffer.writeBulkString(data: ByteArray) {
    writeBA(data)
}
