package eu.vendeli.rethis.codecs.connection

import eu.vendeli.rethis.shared.decoders.general.IntegerDecoder
import eu.vendeli.rethis.shared.request.connection.ClientUnblockType
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.UnexpectedResponseType
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object ClientUnblockCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$6\r\nCLIENT\r\n$7\r\nUNBLOCK\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        clientId: Long,
        unblockType: ClientUnblockType?,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 2
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeLongArg(clientId, charset, )
        unblockType?.let { it0 ->
            size += 1
            buffer.writeStringArg(it0.toString(), charset)
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        clientId: Long,
        unblockType: ClientUnblockType?,
    ): CommandRequest = encode(charset, clientId = clientId, unblockType = unblockType)

    public suspend fun decode(input: Buffer, charset: Charset): Boolean {
        val code = input.parseCode(RespCode.INTEGER)
        return when(code) {
            RespCode.INTEGER -> {
                IntegerDecoder.decode(input, charset, code) == 1L
            }
            else -> {
                throw UnexpectedResponseType("Expected [INTEGER] but got $code", input.tryInferCause(code))
            }
        }
    }
}
