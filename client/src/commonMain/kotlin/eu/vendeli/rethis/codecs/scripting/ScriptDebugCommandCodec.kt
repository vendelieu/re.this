package eu.vendeli.rethis.codecs.scripting

import eu.vendeli.rethis.shared.decoders.general.SimpleStringDecoder
import eu.vendeli.rethis.shared.request.scripting.ScriptDebugMode
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

public object ScriptDebugCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("*3\r\n$6\r\nSCRIPT\r\n$5\r\nDEBUG\r\n")
    }

    public suspend fun encode(charset: Charset, mode: ScriptDebugMode): CommandRequest {
        val buffer = Buffer()
        COMMAND_HEADER.copyTo(buffer)
        buffer.writeStringArg(mode.toString(), charset)

        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(charset: Charset, mode: ScriptDebugMode): CommandRequest = encode(charset, mode = mode)

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
