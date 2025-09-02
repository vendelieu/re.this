package eu.vendeli.rethis.codecs.cluster

import eu.vendeli.rethis.shared.decoders.general.SimpleStringDecoder
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

public object ClusterMeetCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$7\r\nCLUSTER\r\n$4\r\nMEET\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        ip: String,
        port: Long,
        clusterBusPort: Long?,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 2
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(ip, charset, )
        size += 1
        buffer.writeLongArg(port, charset, )
        clusterBusPort?.let { it0 ->
            size += 1
            buffer.writeLongArg(it0, charset, )
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        ip: String,
        port: Long,
        clusterBusPort: Long?,
    ): CommandRequest = encode(charset, ip = ip, port = port, clusterBusPort = clusterBusPort)

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
