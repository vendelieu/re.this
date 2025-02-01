package eu.vendeli.rethis.utils.response

import eu.vendeli.rethis.ResponseParsingException
import eu.vendeli.rethis.types.core.*
import eu.vendeli.rethis.types.core.ResponseToken.Code
import eu.vendeli.rethis.types.core.ResponseToken.Data
import eu.vendeli.rethis.utils.Const.CARRIAGE_RETURN_BYTE
import eu.vendeli.rethis.utils.Const.NEWLINE_BYTE
import eu.vendeli.rethis.utils.cast
import io.ktor.utils.io.*
import kotlinx.io.Source
import kotlinx.io.readDecimalLong

internal suspend fun ByteReadChannel.parseResponse(): ArrayDeque<ResponseToken> {
    val response = ArrayDeque<ResponseToken>()
    val stack = ArrayDeque<Long>() // Stack to manage aggregate sizes

    val line = readLineCRLF()
    val code = RespCode.fromCode(line.readByte())
    parseToken(response, stack, line, code)

    return response
}

internal inline fun <reified L, reified R> RType.unwrapRespIndMap(): Map<L, R?>? =
    if (this is RArray) cast<RArray>().value.chunked(2).associate {
        it.first().unwrap<L>()!! to it.last().unwrap<R>()
    } else unwrapMap<L, R>()

@Suppress("NOTHING_TO_INLINE")
internal inline fun ArrayDeque<ResponseToken>.validatedResponseType(): Code {
    val typeToken = removeFirst()
    if (typeToken !is Code) throw ResponseParsingException(
        message = "Invalid response structure, wrong head token, expected type token but given $typeToken",
    )
    return typeToken
}

@Throws(ResponseParsingException::class)
@Suppress("NOTHING_TO_INLINE")
internal inline fun ArrayDeque<ResponseToken>.validatedSimpleResponse(codeToken: Code): Source {
    if (!codeToken.code.isSimple) throw ResponseParsingException(
        message = "Wrong response type, expected simple type, given ${codeToken.code}",
    )

    if (codeToken.code != RespCode.NULL && isEmpty()) throw ResponseParsingException(
        message = "Invalid response structure, expected data token, given $codeToken",
    )
    val dataToken = removeFirst()

    if (dataToken !is Data) throw ResponseParsingException(
        message = "Invalid response structure, expected data token, given $dataToken",
    )

    return dataToken.buffer
}

private suspend fun ByteReadChannel.parseToken(
    response: ArrayDeque<ResponseToken>,
    stack: ArrayDeque<Long>,
    line: Source,
    code: RespCode,
) {
    when (code.type) {
        RespCode.Type.SIMPLE -> {
            response.addLast(Code(code))
            response.addLast(Data(line))
        }

        RespCode.Type.SIMPLE_AGG -> {
            val size = line.readDecimalLong()
            response.addLast(Code(code, size.toInt()))
            if (size > 0) {
                response.addLast(Data(readRemaining(size)))
                readShort() // skip CRLF
            }
        }

        RespCode.Type.AGGREGATE -> {
            val size = line.readDecimalLong()
            response.addLast(Code(code, size.toInt()))
            stack.addLast(if (code == RespCode.MAP) size * 2 else size) // Push the size onto the stack
        }
    }

    // Process nested aggregates if there are any
    processNestedAggregates(response, stack)
}

private suspend fun ByteReadChannel.processNestedAggregates(
    response: ArrayDeque<ResponseToken>,
    stack: ArrayDeque<Long>,
) {
    while (stack.isNotEmpty()) {
        val currentSize = stack.last()
        if (currentSize > 0) {
            // Read the next segment
            val nestedLine = readLineCRLF()
            val nestedCode = RespCode.fromCode(nestedLine.readByte())

            // Decrement the current size in the stack
            stack[stack.lastIndex] = currentSize - 1
            parseToken(response, stack, nestedLine, nestedCode)
        } else {
            stack.removeLast() // Pop the stack when done
        }
    }
}

private suspend inline fun ByteReadChannel.readLineCRLF(): kotlinx.io.Buffer {
    val buffer = kotlinx.io.Buffer()
    while (true) {
        val byte = readByte()

        if (byte == CARRIAGE_RETURN_BYTE) {
            val nextByte = readByte()
            if (nextByte == NEWLINE_BYTE) {
                break
            } else {
                buffer.writeByte(CARRIAGE_RETURN_BYTE)
                buffer.writeByte(nextByte)
                continue
            }
        }
        buffer.writeByte(byte)
    }
    return buffer
}
