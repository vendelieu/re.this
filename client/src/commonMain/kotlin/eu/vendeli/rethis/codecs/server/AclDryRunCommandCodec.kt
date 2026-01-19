package eu.vendeli.rethis.codecs.server

import eu.vendeli.rethis.shared.decoders.general.BulkStringDecoder
import eu.vendeli.rethis.shared.decoders.general.SimpleStringDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.UnexpectedResponseType
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object AclDryRunCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$3\r\nACL\r\n$6\r\nDRYRUN\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        username: String,
        command: String,
        vararg arg: String,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 2
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(username, charset)
        size += 1
        buffer.writeStringArg(command, charset)
        arg.forEach { it0 ->
            size += 1
            buffer.writeStringArg(it0, charset)
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        username: String,
        command: String,
        vararg arg: String,
    ): CommandRequest = encode(charset, username = username, command = command, arg = arg)

    public suspend fun decode(input: Buffer, charset: Charset): String {
        val code = input.parseCode(RespCode.BULK)
        return when (code) {
            RespCode.BULK -> {
                BulkStringDecoder.decode(input, charset, code)
            }

            RespCode.SIMPLE_STRING -> {
                SimpleStringDecoder.decode(input, charset, code)
            }

            else -> {
                throw UnexpectedResponseType("Expected [BULK, SIMPLE_STRING] but got $code", input.tryInferCause(code))
            }
        }
    }
}
