package eu.vendeli.rethis.utils

import eu.vendeli.rethis.types.core.PairArgument
import eu.vendeli.rethis.types.core.RespCode
import eu.vendeli.rethis.types.core.TripleArgument
import eu.vendeli.rethis.utils.Const.EOL
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.readByteArray

@Suppress("NOTHING_TO_INLINE")
internal inline fun Buffer.writeValues(value: Any?, forceBulk: Boolean = true) = apply {
    writeRedisValue(value, forceBulk)
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun bufferValues(value: Any?, forceBulk: Boolean = true) = Buffer().writeValues(value, forceBulk)

internal fun Sink.writeRedisValue(
    value: Any?,
    forceBulk: Boolean = true,
    charset: Charset = Charsets.UTF_8,
): Sink = apply {
    when {
        value is List<*> -> writeListValue(value, forceBulk, charset)
        value is Array<*> -> writeArrayValue(value, forceBulk, charset)
        value is ByteArray -> writeByteArray(value)
        value is Pair<*, *> -> {
            writeRedisValue(value.first, forceBulk, charset)
            writeRedisValue(value.second, forceBulk, charset)
        }

        value is PairArgument<*, *> -> {
            writeRedisValue(value.arg.first, forceBulk, charset)
            writeRedisValue(value.arg.second, forceBulk, charset)
        }

        value is TripleArgument<*, *, *> -> {
            writeRedisValue(value.arg.first, forceBulk, charset)
            writeRedisValue(value.arg.second, forceBulk, charset)
            writeRedisValue(value.arg.third, forceBulk, charset)
        }

        forceBulk -> writeBulk(value, charset)
        value is String -> writeString(value, charset)
        value is Int || value is Long -> writeIntegral(value)
        value is Double -> writeDouble(value)
        value == null -> writeNull()
        value is Throwable -> writeThrowable(value)
        else -> error("Unsupported $value to write")
    }
}

private fun <T : List<*>> Sink.writeListValue(
    value: T,
    forceBulk: Boolean = true,
    charset: Charset = Charsets.UTF_8,
) {
    append(RespCode.ARRAY)
    var sizeAdjustment = 0
    value.forEach {
        if (it is PairArgument<*, *> || it is Pair<*, *>) sizeAdjustment++
        if (it is TripleArgument<*, *, *>) sizeAdjustment += 2
    }

    append(value.size.plus(sizeAdjustment).toString())
    appendEOL()

    for (item in value) writeRedisValue(item, forceBulk, charset)
}

private fun Sink.writeArrayValue(
    value: Array<*>,
    forceBulk: Boolean = true,
    charset: Charset = Charsets.UTF_8,
) {
    append(RespCode.ARRAY)
    append(value.size.toString())
    appendEOL()
    for (item in value) writeRedisValue(item, forceBulk, charset)
}

private fun Sink.writeBulk(value: Any?, charset: Charset) {
    val packet = buildPacket {
        writeStringEncoded(value.toString(), charset = charset)
    }
    append(RespCode.BULK)
    append(packet.remaining.toString())
    appendEOL()
    writePacket(packet)
    appendEOL()
}

private fun Sink.writeByteArray(value: ByteArray) {
    append(RespCode.BULK)
    append(value.size.toString())
    appendEOL()
    writeFully(value)
    appendEOL()
}

private fun Sink.writeString(value: String, charset: Charset) {
    if (value.contains('\n') || value.contains('\r')) {
        val packet = buildPacket { writeStringEncoded(value, charset) }
        append(RespCode.BULK)
        append(packet.remaining.toString())
        appendEOL()
        writePacket(packet)
        appendEOL()
        return
    }

    append("+")
    writeStringEncoded(value, charset)
    appendEOL()
}

private fun Sink.writeIntegral(value: Any) {
    append(RespCode.INTEGER)
    append(value.toString())
    appendEOL()
}

private fun Sink.writeDouble(value: Any) {
    append(RespCode.DOUBLE)
    append(value.toString())
    appendEOL()
}

private fun Sink.writeNull() {
    append(RespCode.BULK)
    append("-1")
    appendEOL()
}

private fun Sink.writeThrowable(value: Throwable) {
    val message = (value.message ?: "Error")
        .replace("\r", "")
        .replace("\n", "")

    append(RespCode.SIMPLE_ERROR)
    append(message)
    appendEOL()
}

private fun Sink.append(type: RespCode) {
    writeByte(type.code)
}

private fun Sink.appendEOL() {
    writeFully(EOL)
}

private fun Sink.writeStringEncoded(string: String, charset: Charset) {
    writeFully(charset.newEncoder().encode(string).readByteArray())
}
