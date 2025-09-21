package eu.vendeli.rethis.codecs.connection

import eu.vendeli.rethis.shared.decoders.general.BulkStringDecoder
import eu.vendeli.rethis.shared.request.connection.ClientType
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

public object ClientListCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$6\r\nCLIENT\r\n$4\r\nLIST\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        clientType: ClientType?,
        vararg clientId: Long,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 2
        COMMAND_HEADER.copyTo(buffer)
        clientType?.let { it0 ->
            when (it0) {
                is ClientType ->  {
                    size += 1
                    buffer.writeStringArg("TYPE", charset)
                    when (it0) {
                        is ClientType.Slave ->  {
                        }
                        is ClientType.Normal ->  {
                            size += 1
                            buffer.writeStringArg("NORMAL", charset)
                        }
                        is ClientType.Master ->  {
                            size += 1
                            buffer.writeStringArg("MASTER", charset)
                        }
                        is ClientType.Replica ->  {
                            size += 1
                            buffer.writeStringArg("REPLICA", charset)
                        }
                        is ClientType.PubSub ->  {
                            size += 1
                            buffer.writeStringArg("PUBSUB", charset)
                        }
                    }
                }
            }
        }
        if (clientId.isNotEmpty()) {
            size += 1
            buffer.writeStringArg("ID", charset)
        }
        clientId.forEach { it1 ->
            size += 1
            buffer.writeLongArg(it1, charset, )
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        clientType: ClientType?,
        vararg clientId: Long,
    ): CommandRequest = encode(charset, clientType = clientType, clientId = clientId)

    public suspend fun decode(input: Buffer, charset: Charset): String {
        val code = input.parseCode(RespCode.BULK)
        return when(code) {
            RespCode.BULK -> {
                BulkStringDecoder.decode(input, charset, code)
            }
            else -> {
                throw UnexpectedResponseType("Expected [BULK] but got $code", input.tryInferCause(code))
            }
        }
    }
}
