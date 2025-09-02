package eu.vendeli.rethis.codecs.connection

import eu.vendeli.rethis.shared.decoders.aggregate.MapRTypeDecoder
import eu.vendeli.rethis.shared.decoders.general.SimpleErrorDecoder
import eu.vendeli.rethis.shared.request.connection.HelloAuth
import eu.vendeli.rethis.shared.types.*
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeCharArrayArg
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object HelloCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$5\r\nHELLO\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        protover: Long?,
        auth: HelloAuth?,
        clientname: String?,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        protover?.let { it0 ->
            size += 1
            buffer.writeLongArg(it0, charset, )
        }
        auth?.let { it1 ->
            size += 1
            buffer.writeStringArg("AUTH", charset)
            size += 1
            buffer.writeStringArg(it1.username, charset, )
            size += 1
            buffer.writeCharArrayArg(it1.password, charset, )
        }
        clientname?.let { it2 ->
            size += 1
            buffer.writeStringArg("SETNAME", charset)
            size += 1
            buffer.writeStringArg(it2, charset, )
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        protover: Long?,
        auth: HelloAuth?,
        clientname: String?,
    ): CommandRequest = encode(charset, protover = protover, auth = auth, clientname = clientname)

    public suspend fun decode(input: Buffer, charset: Charset): Map<String, RType> {
        val code = input.parseCode(RespCode.MAP)
        return when(code) {
            RespCode.MAP -> {
                MapRTypeDecoder.decode(input, charset, code)
            }
            RespCode.ARRAY -> {
                MapRTypeDecoder.decode(input, charset, code)
            }
            RespCode.SIMPLE_ERROR -> {
                SimpleErrorDecoder.decode(input, charset, code)
            }
            else -> {
                throw UnexpectedResponseType("Expected [MAP, ARRAY, SIMPLE_ERROR] but got $code", input.tryInferCause(code))
            }
        }
    }
}
