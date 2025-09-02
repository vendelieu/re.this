package eu.vendeli.rethis.codecs.sentinel

import eu.vendeli.rethis.shared.decoders.general.SimpleStringDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.UnexpectedResponseType
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeIntArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object SentinelMonitorCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("*6\r\n$8\r\nSENTINEL\r\n$7\r\nMONITOR\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        masterName: String,
        ip: String,
        port: Int,
        quorum: Int,
    ): CommandRequest {
        val buffer = Buffer()
        COMMAND_HEADER.copyTo(buffer)
        buffer.writeStringArg(masterName, charset, )
        buffer.writeStringArg(ip, charset, )
        buffer.writeIntArg(port, charset, )
        buffer.writeIntArg(quorum, charset, )

        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        masterName: String,
        ip: String,
        port: Int,
        quorum: Int,
    ): CommandRequest = encode(charset, masterName = masterName, ip = ip, port = port, quorum = quorum)

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
