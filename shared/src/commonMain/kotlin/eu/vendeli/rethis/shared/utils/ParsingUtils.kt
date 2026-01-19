package eu.vendeli.rethis.shared.utils

import eu.vendeli.rethis.shared.decoders.general.BulkStringDecoder
import eu.vendeli.rethis.shared.decoders.general.VerbatimStringDecoder
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.RespProtocolException
import eu.vendeli.rethis.shared.types.RespUnexpectedEOF
import eu.vendeli.rethis.shared.types.ResponseParsingException
import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.InternalIoApi
import kotlin.jvm.JvmName

val EMPTY_BUFFER = Buffer()
internal val EMPTY_BYTE_ARRAY = ByteArray(0)

@Suppress("UNCHECKED_CAST")
internal inline fun <reified R> Any?.safeCast(): R? = this as? R

@Suppress("UNCHECKED_CAST")
internal inline fun <reified R> Any?.cast(): R = this as R

internal suspend inline fun MutableCollection<String>.parseStrings(size: Int, input: Buffer, charset: Charset) {
    repeat(size) {
        when (val code = RespCode.fromCode(input.readByte())) {
            RespCode.BULK -> add(
                BulkStringDecoder.decode(input, charset, code),
            )

            RespCode.VERBATIM_STRING -> add(
                VerbatimStringDecoder.decode(input, charset, code),
            )

            else -> throw ResponseParsingException(
                "Invalid response structure, expected string token, given $code",
                input.tryInferCause(code),
            )
        }
    }
}

@JvmName("parseStringsNullable")
internal suspend inline fun MutableCollection<String?>.parseStrings(size: Int, input: Buffer, charset: Charset) {
    repeat(size) {
        when (val code = RespCode.fromCode(input.readByte())) {
            RespCode.NULL -> add(null)

            RespCode.BULK -> add(BulkStringDecoder.decodeNullable(input, charset, code))

            RespCode.VERBATIM_STRING -> add(VerbatimStringDecoder.decodeNullable(input, charset, code))

            else -> throw ResponseParsingException(
                "Invalid response structure, expected string token, given $code",
                input.tryInferCause(code),
            )
        }
    }
}

internal inline fun Buffer.resolveToken(requiredToken: RespCode): RespCode {
    val code = RespCode.fromCode(readByte())
    if (code != requiredToken) throw ResponseParsingException(
        "Invalid response structure, expected ${requiredToken.name} token, given $code", tryInferCause(code),
    )

    return code
}

private data class Frame(
    var remaining: Int,
)

@OptIn(InternalAPI::class, InternalIoApi::class)
suspend fun ByteReadChannel.readCompleteResponseInto(
    target: Buffer,
) {
    // Read first type byte
    val firstType = readByteRequired(target)
    val firstCode = RespCode.fromCode(firstType)

    val stack = ArrayDeque<Frame>()

    // Top-level always expects exactly 1 element
    stack.addLast(Frame(remaining = 1))

    // We already consumed the first type byte
    processValue(
        out = target,
        code = firstCode,
        stack = stack,
    )

    // When stack empties, exactly one frame is read
    check(stack.isEmpty()) {
        "RESP framing invariant violated: stack not empty at end"
    }
}

@OptIn(InternalAPI::class, InternalIoApi::class)
private suspend fun ByteReadChannel.processValue(
    out: Buffer,
    code: RespCode,
    stack: ArrayDeque<Frame>,
) {
    when (code.type) {
        RespCode.Type.SIMPLE -> {
            // + - : # , ( _
            when (code) {
                RespCode.NULL -> {
                    // "_\r\n"
                    readCrlfRequired(out)
                }

                else -> {
                    // Simple line
                    readUntilCrlfInto(out)
                }
            }
            onElementComplete(stack)
        }

        RespCode.Type.SIMPLE_AGG -> {
            // $ ! =
            readBulkLike(out)
            onElementComplete(stack)
        }

        RespCode.Type.AGGREGATE -> {
            readAggregateHeader(out, code, stack)
        }
    }

    // Continue while there are pending aggregate elements
    while (stack.isNotEmpty()) {
        val frame = stack.last()
        if (frame.remaining == 0) {
            stack.removeLast()
            onElementComplete(stack)
            continue
        }

        // Read next element type
        val nextType = readByteRequired(out)
        val nextCode = RespCode.fromCode(nextType)

        processValue(
            out = out,
            code = nextCode,
            stack = stack,
        )
    }
}

@OptIn(InternalAPI::class, InternalIoApi::class)
private suspend fun ByteReadChannel.readAggregateHeader(
    out: Buffer,
    code: RespCode,
    stack: ArrayDeque<Frame>,
) {
    // Read "<count>\r\n"
    val count = readDecimalLong(out)

    if (count < -1) {
        throw RespProtocolException("Invalid aggregate size: $count")
    }

    // Null aggregate
    if (count == -1L) {
        onElementComplete(stack)
        return
    }

    val elements = when (code) {
        RespCode.MAP, RespCode.ATTRIBUTE -> count * 2
        else -> count
    }

    if (elements > Int.MAX_VALUE) {
        throw RespProtocolException("Aggregate too large: $elements")
    }

    stack.addLast(Frame(elements.toInt()))
}

@OptIn(InternalAPI::class, InternalIoApi::class)
private suspend fun ByteReadChannel.readBulkLike(
    out: Buffer,
) {
    val length = readDecimalLong(out)

    if (length < -1) {
        throw RespProtocolException("Invalid bulk length: $length")
    }

    if (length == -1L) {
        // Null bulk
        return
    }

    // Read payload
    readFullyRequired(out, length)

    // Read trailing CRLF
    readCrlfRequired(out)
}

private fun onElementComplete(stack: ArrayDeque<Frame>) {
    if (stack.isEmpty()) return
    stack.last().remaining--
}

@OptIn(InternalAPI::class, InternalIoApi::class)
private suspend fun ByteReadChannel.readByteRequired(out: Buffer): Byte {
    if (readBuffer.buffer.size == 0L) awaitContent()
    if (readBuffer.buffer.size == 0L) throw RespUnexpectedEOF()
    val b = readBuffer.buffer.readByte()
    out.writeByte(b)
    return b
}

@OptIn(InternalAPI::class, InternalIoApi::class)
private suspend fun ByteReadChannel.readUntilCrlfInto(out: Buffer) {
    while (true) {
        if (readBuffer.buffer.size == 0L) awaitContent()
        if (readBuffer.buffer.size == 0L) throw RespUnexpectedEOF()

        val b = readBuffer.buffer.readByte()
        out.writeByte(b)

        if (b == CARRIAGE_RETURN_BYTE) {
            // Expect LF next
            if (readBuffer.buffer.size == 0L) awaitContent()
            if (readBuffer.buffer.size == 0L) throw RespUnexpectedEOF()

            val lf = readBuffer.buffer.readByte()
            out.writeByte(lf)

            if (lf != NEWLINE_BYTE) {
                throw RespProtocolException("Invalid CRLF sequence")
            }
            return
        }
    }
}

@OptIn(InternalAPI::class, InternalIoApi::class)
private suspend fun ByteReadChannel.readCrlfRequired(out: Buffer) {
    if (readBuffer.buffer.size == 0L) awaitContent()
    if (readBuffer.buffer.size == 0L) throw RespUnexpectedEOF()
    val cr = readBuffer.buffer.readByte()

    if (readBuffer.buffer.size == 0L) awaitContent()
    if (readBuffer.buffer.size == 0L) throw RespUnexpectedEOF()
    val lf = readBuffer.buffer.readByte()

    if (cr != CARRIAGE_RETURN_BYTE || lf != NEWLINE_BYTE) {
        throw RespProtocolException("Expected CRLF")
    }

    out.writeByte(cr)
    out.writeByte(lf)
}

@OptIn(InternalAPI::class, InternalIoApi::class)
private suspend fun ByteReadChannel.readDecimalLong(out: Buffer): Long {
    var negative = false
    var value = 0L
    var readAny = false

    while (true) {
        if (readBuffer.buffer.size == 0L) awaitContent()
        if (readBuffer.buffer.size == 0L) throw RespUnexpectedEOF()
        val b = readBuffer.buffer.readByte()
        out.writeByte(b)

        when (b) {
            '-'.code.toByte() -> {
                if (readAny) throw RespProtocolException("Unexpected '-' in number")
                negative = true
            }

            CARRIAGE_RETURN_BYTE -> {
                if (readBuffer.buffer.size == 0L) awaitContent()
                if (readBuffer.buffer.size == 0L) throw RespUnexpectedEOF()
                val lf = readBuffer.buffer.readByte()
                out.writeByte(lf)
                if (lf != NEWLINE_BYTE) {
                    throw RespProtocolException("Invalid CRLF in number")
                }
                return if (negative) -value else value
            }

            in '0'.code.toByte()..'9'.code.toByte() -> {
                readAny = true
                value = value * 10 + (b - '0'.code.toByte())
            }

            else -> throw RespProtocolException(
                "Invalid digit in number: ${b.toInt().toChar()}",
            )
        }
    }
}

@OptIn(InternalAPI::class, InternalIoApi::class)
private suspend fun ByteReadChannel.readFullyRequired(out: Buffer, length: Long) {
    var remaining = length
    while (remaining > 0) {
        if (readBuffer.buffer.size == 0L) awaitContent()
        if (readBuffer.buffer.size == 0L) throw RespUnexpectedEOF()
        val toRead = minOf(remaining, readBuffer.buffer.size)
        out.write(readBuffer.buffer, toRead)
        remaining -= toRead
    }
}
