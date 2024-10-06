package eu.vendeli.rethis.utils

import eu.vendeli.rethis.types.core.*
import eu.vendeli.rethis.utils.Const.EOL
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.Sink

@Suppress("NOTHING_TO_INLINE")
internal inline fun Buffer.writeValues(value: List<Argument>, charset: Charset) = apply {
    writeRedisValue(value, charset)
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun bufferValues(value: List<Argument>, charset: Charset) = Buffer().writeValues(value, charset)

internal fun Sink.writeRedisValue(
    data: Any?,
    charset: Charset = Charsets.UTF_8,
): Sink = apply {
    when (data) {
        is List<*> -> writeListValue(data, charset)
        is Array<*> -> writeArrayValue(data, charset)

        is StringArg -> writeByteArray(data.value.toByteArray(charset))
        is LongArg -> writeByteArray(data.value.toString().toByteArray(charset))
        is IntArg -> writeByteArray(data.value.toString().toByteArray(charset))
        is DoubleArg -> writeByteArray(data.value.toString().toByteArray(charset))
        is BaArg -> writeByteArray(data.value)
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun <T, R : Argument> MutableList<R>.writeArg(value: List<T>): MutableList<R> {
    if (isEmpty()) return this

    value.forEach { writeArg(it) }
    return this
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun <T, R : Argument> MutableList<R>.writeArg(value: Array<T>): MutableList<R> {
    if (isEmpty()) return this

    value.forEach { writeArg(it) }
    return this
}

@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
internal fun <T, R : Argument> MutableList<R>.writeArg(value: T): MutableList<R> {
    if (value == null) return this

    when (value) {
        is VaryingArgument -> {
            writeArg(value.data)
        }

        is Pair<*, *> -> {
            value.first?.also { add(it.toArg() as R) }
            value.second?.also { add(it.toArg() as R) }
        }

        else -> add(value.toArg() as R)
    }

    return this
}

private fun <T : List<*>> Sink.writeListValue(
    value: T,
    charset: Charset = Charsets.UTF_8,
) {
    append(RespCode.ARRAY)
    append(value.size.toString())
    appendEOL()

    for (item in value) writeRedisValue(item, charset)
}

private fun Sink.writeArrayValue(
    value: Array<*>,
    charset: Charset = Charsets.UTF_8,
) {
    append(RespCode.ARRAY)
    append(value.size.toString())
    appendEOL()
    for (item in value) writeRedisValue(item, charset)
}

private fun Sink.writeByteArray(value: ByteArray) {
    append(RespCode.BULK)
    append(value.size.toString())
    appendEOL()
    writeFully(value)
    appendEOL()
}

private fun Sink.append(type: RespCode) {
    writeByte(type.code)
}

private fun Sink.appendEOL() {
    writeFully(EOL)
}
