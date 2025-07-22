package eu.vendeli.rethis.codecs.connection

import eu.vendeli.rethis.api.spec.common.decoders.general.BulkStringDecoder
import eu.vendeli.rethis.api.spec.common.request.connection.ClientType
import eu.vendeli.rethis.api.spec.common.request.connection.ClientType.Master
import eu.vendeli.rethis.api.spec.common.request.connection.ClientType.Normal
import eu.vendeli.rethis.api.spec.common.request.connection.ClientType.PubSub
import eu.vendeli.rethis.api.spec.common.request.connection.ClientType.Replica
import eu.vendeli.rethis.api.spec.common.request.connection.ClientType.Slave
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.Long
import kotlin.String
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
                is ClientType.Master ->  {
                    size += 1
                    buffer.writeStringArg("MASTER", charset)
                }
                is ClientType.Normal ->  {
                    size += 1
                    buffer.writeStringArg("NORMAL", charset)
                }
                is ClientType.PubSub ->  {
                    size += 1
                    buffer.writeStringArg("PUBSUB", charset)
                }
                is ClientType.Replica ->  {
                    size += 1
                    buffer.writeStringArg("REPLICA", charset)
                }
                is ClientType.Slave ->  {
                }
            }
        }
        clientId.forEach { it1 ->
            size += 1
            buffer.writeStringArg("ID", charset)
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
