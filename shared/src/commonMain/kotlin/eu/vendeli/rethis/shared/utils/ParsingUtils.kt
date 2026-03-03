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

private const val MAX_NESTING_DEPTH = 64 // Limit for RESP nesting (incl. ATTRIBUTE maps and deep replies)

val EMPTY_BUFFER = Buffer()

const val BYTE_MINUS = '-'.code.toByte()
const val BYTE_0 = '0'.code.toByte()
const val BYTE_9 = '9'.code.toByte()
internal val EMPTY_BYTE_ARRAY = ByteArray(0)

@Suppress("UNCHECKED_CAST")
internal inline fun <reified R> Any?.safeCast(): R? = this as? R

@Suppress("UNCHECKED_CAST")
internal inline fun <reified R> Any?.cast(): R = this as R

internal inline fun MutableCollection<String>.parseStrings(size: Int, input: Buffer, charset: Charset) {
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
internal inline fun MutableCollection<String?>.parseStrings(size: Int, input: Buffer, charset: Charset) {
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

@OptIn(InternalAPI::class, InternalIoApi::class)
suspend fun ByteReadChannel.readCompleteResponseInto(
    target: Buffer,
    attributesOut: Buffer? = null,
) {
    // Read first type byte without writing to target (so we can route ATTRIBUTE to attributesOut)
    val firstType = readByteOnly()
    val firstCode = RespCode.fromCode(firstType)

    // Stack as IntArray: each element is the remaining count for that nesting level
    val stack = IntArray(MAX_NESTING_DEPTH)
    var stackSize = 1
    stack[0] = 1 // Top-level expects exactly 1 element

    val finalStackSize = if (firstCode == RespCode.ATTRIBUTE) {
        // RESP3: attribute map followed by the actual reply — consume attribute, then read reply into target
        val attrBuffer = attributesOut ?: Buffer()
        attrBuffer.writeByte(firstType)
        val afterAttr = processValue(
            out = attrBuffer,
            initialCode = firstCode,
            stack = stack,
            initialStackSize = stackSize,
        )
        check(afterAttr == 0) { "RESP framing invariant violated: attribute map did not consume fully" }
        // Read the next value (actual reply) into target
        val nextType = readByteRequired(target)
        val nextCode = RespCode.fromCode(nextType)
        stack[0] = 1
        processValue(
            out = target,
            initialCode = nextCode,
            stack = stack,
            initialStackSize = 1,
        )
    } else {
        target.writeByte(firstType)
        processValue(
            out = target,
            initialCode = firstCode,
            stack = stack,
            initialStackSize = stackSize,
        )
    }

    // When stack empties, exactly one frame is read
    check(finalStackSize == 0) {
        "RESP framing invariant violated: stack not empty at end"
    }
}

@OptIn(InternalAPI::class, InternalIoApi::class)
private suspend fun ByteReadChannel.processValue(
    out: Buffer,
    initialCode: RespCode,
    stack: IntArray,
    initialStackSize: Int,
): Int {
    var stackSize = initialStackSize
    var currentCode: RespCode? = initialCode

    while (true) {
        if (currentCode == null) {
            if (stackSize == 0) return 0
            if (stack[stackSize - 1] == 0) {
                stackSize--
                stackSize = onElementComplete(stack, stackSize)
                continue
            }
            val nextType = readByteRequired(out)
            currentCode = RespCode.fromCode(nextType)
        }

        val code = currentCode!!
        currentCode = null

        when (code.type) {
            RespCode.Type.SIMPLE -> {
                when (code) {
                    RespCode.NULL -> readCrlfRequired(out)
                    else -> readUntilCrlfInto(out)
                }
                stackSize = onElementComplete(stack, stackSize)
            }
            RespCode.Type.SIMPLE_AGG -> {
                readBulkLike(out)
                stackSize = onElementComplete(stack, stackSize)
            }
            RespCode.Type.AGGREGATE -> {
                stackSize = readAggregateHeader(out, code, stack, stackSize)
            }
        }
    }
}

@OptIn(InternalAPI::class, InternalIoApi::class)
private suspend fun ByteReadChannel.readAggregateHeader(
    out: Buffer,
    code: RespCode,
    stack: IntArray,
    stackSize: Int,
): Int {
    val count = readDecimalLong(out)

    if (count < -1) {
        throw RespProtocolException("Invalid aggregate size: $count")
    }

    // Null aggregate
    if (count == -1L) {
        return onElementComplete(stack, stackSize)
    }

    val elements = when (code) {
        RespCode.MAP, RespCode.ATTRIBUTE -> count * 2
        else -> count
    }

    if (elements > Int.MAX_VALUE) {
        throw RespProtocolException("Aggregate too large: $elements")
    }

    if (stackSize >= stack.size) {
        throw RespProtocolException("RESP nesting too deep: $stackSize")
    }

    stack[stackSize] = elements.toInt()
    return stackSize + 1
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

private fun onElementComplete(stack: IntArray, stackSize: Int): Int {
    if (stackSize == 0) return 0
    stack[stackSize - 1]--
    return stackSize
}

@OptIn(InternalAPI::class, InternalIoApi::class)
private suspend fun ByteReadChannel.readUntilCrlfInto(out: Buffer) {
    while (true) {
        if (readBuffer.exhausted()) awaitContent()
        if (readBuffer.exhausted()) throw RespUnexpectedEOF()

        val b = readBuffer.buffer.readByte()
        out.writeByte(b)

        if (b == CARRIAGE_RETURN_BYTE) {
            // Expect LF next
            if (readBuffer.exhausted()) awaitContent()
            if (readBuffer.exhausted()) throw RespUnexpectedEOF()

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
    if (readBuffer.exhausted()) awaitContent()
    if (readBuffer.exhausted()) throw RespUnexpectedEOF()
    val cr = readBuffer.buffer.readByte()

    if (readBuffer.exhausted()) awaitContent()
    if (readBuffer.exhausted()) throw RespUnexpectedEOF()
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
        if (readBuffer.exhausted()) awaitContent()
        if (readBuffer.exhausted()) throw RespUnexpectedEOF()
        val b = readBuffer.buffer.readByte()
        out.writeByte(b)

        when (b) {
            BYTE_MINUS -> {
                if (readAny) throw RespProtocolException("Unexpected '-' in number")
                negative = true
            }

            CARRIAGE_RETURN_BYTE -> {
                if (readBuffer.exhausted()) awaitContent()
                if (readBuffer.exhausted()) throw RespUnexpectedEOF()
                val lf = readBuffer.buffer.readByte()
                out.writeByte(lf)
                if (lf != NEWLINE_BYTE) {
                    throw RespProtocolException("Invalid CRLF in number")
                }
                return if (negative) -value else value
            }

            in BYTE_0..BYTE_9 -> {
                readAny = true
                value = value * 10 + (b - BYTE_0)
            }

            else -> throw RespProtocolException(
                "Invalid digit in number: ${b.toInt().toChar()}",
            )
        }
    }
}

@OptIn(InternalAPI::class, InternalIoApi::class)
private suspend fun ByteReadChannel.readByteOnly(): Byte {
    if (readBuffer.exhausted()) awaitContent()
    if (readBuffer.exhausted()) throw RespUnexpectedEOF()
    return readBuffer.buffer.readByte()
}

@OptIn(InternalAPI::class, InternalIoApi::class)
private suspend fun ByteReadChannel.readByteRequired(out: Buffer): Byte {
    if (readBuffer.exhausted()) awaitContent()
    if (readBuffer.exhausted()) throw RespUnexpectedEOF()

    val b = readBuffer.buffer.readByte()
    out.writeByte(b)
    return b
}

@OptIn(InternalAPI::class, InternalIoApi::class)
private suspend fun ByteReadChannel.readFullyRequired(out: Buffer, length: Long) {
    var remaining = length
    while (remaining > 0) {
        if (readBuffer.exhausted()) awaitContent()
        if (readBuffer.exhausted()) throw RespUnexpectedEOF()
        val toRead = minOf(remaining, readBuffer.buffer.size)
        out.write(readBuffer.buffer, toRead)
        remaining -= toRead
    }
}
