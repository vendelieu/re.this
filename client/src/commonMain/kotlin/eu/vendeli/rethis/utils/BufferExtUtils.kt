package eu.vendeli.rethis.utils

import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.TimeUnit
import eu.vendeli.rethis.utils.Const.EOL
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
    writeText(value.size.toString(), charset = charset)
    appendEOL()
    writeFully(value)
    appendEOL()
}

fun Buffer.writeStringArg(value: String, charset: Charset) = writeBA(value.toByteArray(charset), charset)
fun Buffer.writeLongArg(value: Long, charset: Charset) = writeBA(value.toString().toByteArray(charset), charset)
fun Buffer.writeIntArg(value: Int, charset: Charset) = writeBA(value.toString().toByteArray(charset), charset)
fun Buffer.writeDoubleArg(value: Double, charset: Charset) = writeBA(value.toString().toByteArray(charset), charset)
fun Buffer.writeByteArrayArg(value: ByteArray, charset: Charset) = writeBA(value, charset)
fun <T : List<*>> Buffer.writeListArg(
    value: T,
    charset: Charset = Charsets.UTF_8,
) {
    append(RespCode.ARRAY)
    append(value.size.toString())
    appendEOL()

    for (item in value) writeRedisValue(item, charset)
}

fun Buffer.writeArrayArg(
    value: Array<*>,
    charset: Charset = Charsets.UTF_8,
) {
    append(RespCode.ARRAY)
    append(value.size.toString())
    appendEOL()

    for (item in value) writeRedisValue(item, charset)
}

private fun Buffer.writeRedisValue(
    data: Any?,
    charset: Charset = Charsets.UTF_8,
): Buffer = apply {
    when (data) {
        is List<*> -> writeListArg(data, charset)

        is String -> writeStringArg(data, charset)
        is Long -> writeLongArg(data, charset)
        is Int -> writeIntArg(data, charset)
        is Double -> writeDoubleArg(data, charset)
        is ByteArray -> writeByteArrayArg(data, charset)
    }
}

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
