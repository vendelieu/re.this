package eu.vendeli.rethis.utils

import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.TimeUnit
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.datetime.Instant
import kotlinx.io.Buffer
import kotlin.time.Duration

private fun Buffer.appendEOL() {
    writeFully(EOL)
}

private fun Buffer.append(type: RespCode) {
    writeByte(type.code)
}

private fun Buffer.writeBA(value: ByteArray, charset: Charset) {
    append(RespCode.BULK)
    writeInt(value.size)
    appendEOL()
    writeFully(value)
    appendEOL()
}

fun Buffer.writeStringArg(value: String, charset: Charset) = writeBA(value.toByteArray(charset), charset)
fun Buffer.writeLongArg(value: Long, charset: Charset) = writeBA(value.toString().toByteArray(charset), charset)
fun Buffer.writeIntArg(value: Int, charset: Charset) = writeBA(value.toString().toByteArray(charset), charset)
fun Buffer.writeDoubleArg(value: Double, charset: Charset) = writeBA(value.toString().toByteArray(charset), charset)
fun Buffer.writeByteArrayArg(value: ByteArray, charset: Charset) = writeBA(value, charset)

fun Buffer.writeDurationArg(value: Duration, charset: Charset, timeUnit: TimeUnit) = when (timeUnit) {
    TimeUnit.MILLISECONDS -> writeBA(value.inWholeMilliseconds.toString().toByteArray(charset), charset)
    TimeUnit.SECONDS -> writeBA(value.inWholeSeconds.toString().toByteArray(charset), charset)
}

fun Buffer.writeInstantArg(value: Instant, charset: Charset, timeUnit: TimeUnit) = when (timeUnit) {
    TimeUnit.MILLISECONDS -> writeBA(value.toEpochMilliseconds().toString().toByteArray(charset), charset)
    TimeUnit.SECONDS -> writeBA(value.epochSeconds.toString().toByteArray(charset), charset)
}

fun Buffer.writeBooleanArg(value: Boolean, charset: Charset) =
    writeBA(value.let { if (it) "t" else "f" }.toByteArray(charset), charset)

fun Buffer.writeCharArrayArg(value: CharArray, charset: Charset) {
    append(RespCode.BULK)
    writeInt(value.size)
    appendEOL()
    writeText(value, charset = charset)
    appendEOL()
}
