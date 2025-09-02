package eu.vendeli.rethis.codecs.scripting

import eu.vendeli.rethis.shared.decoders.general.SimpleStringDecoder
import eu.vendeli.rethis.shared.request.scripting.FunctionRestoreOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.UnexpectedResponseType
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeByteArrayArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object FunctionRestoreCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$8\r\nFUNCTION\r\n$7\r\nRESTORE\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        serializedValue: ByteArray,
        policy: FunctionRestoreOption?,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 2
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeByteArrayArg(serializedValue, charset, )
        policy?.let { it0 ->
            when (it0) {
                is FunctionRestoreOption.APPEND ->  {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }
                is FunctionRestoreOption.FLUSH ->  {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }
                is FunctionRestoreOption.REPLACE ->  {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }
            }
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        serializedValue: ByteArray,
        policy: FunctionRestoreOption?,
    ): CommandRequest = encode(charset, serializedValue = serializedValue, policy = policy)

    public suspend fun decode(input: Buffer, charset: Charset): Boolean {
        val code = input.parseCode(RespCode.SIMPLE_STRING)
        return when(code) {
            RespCode.SIMPLE_STRING -> {
                SimpleStringDecoder.decode(input, charset, code) == "OK"
            }
            else -> {
                throw UnexpectedResponseType("Expected [SIMPLE_STRING] but got $code", input.tryInferCause(code))
            }
        }
    }
}
