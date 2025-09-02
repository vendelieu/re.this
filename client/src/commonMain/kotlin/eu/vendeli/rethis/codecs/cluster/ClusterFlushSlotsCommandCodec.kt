package eu.vendeli.rethis.codecs.cluster

import eu.vendeli.rethis.shared.decoders.general.SimpleStringDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.UnexpectedResponseType
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.utils.parseCode
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object ClusterFlushSlotsCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("*2\r\n$7\r\nCLUSTER\r\n$10\r\nFLUSHSLOTS\r\n")
    }

    public suspend fun encode(charset: Charset): CommandRequest {
        val buffer = Buffer()
        COMMAND_HEADER.copyTo(buffer)

        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(charset: Charset): CommandRequest = encode(charset, )

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
