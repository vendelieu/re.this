package eu.vendeli.rethis.codecs.server

import eu.vendeli.rethis.shared.decoders.general.SimpleStringDecoder
import eu.vendeli.rethis.shared.request.server.FailoverOptions
import eu.vendeli.rethis.shared.types.*
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeDurationArg
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object FailoverCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$8\r\nFAILOVER\r\n")
    }

    public suspend fun encode(charset: Charset, vararg option: FailoverOptions): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        option.forEach { it0 ->
            when (it0) {
                is FailoverOptions.ABORT -> {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }

                is FailoverOptions.Timeout -> {
                    size += 1
                    buffer.writeStringArg("TIMEOUT", charset)
                    size += 1
                    buffer.writeDurationArg(it0.milliseconds, charset, TimeUnit.MILLISECONDS)
                }

                is FailoverOptions.To -> {
                    size += 1
                    buffer.writeStringArg("TO", charset)
                    size += 1
                    buffer.writeStringArg(it0.host, charset)
                    size += 1
                    buffer.writeLongArg(it0.port, charset)
                    if (it0.force) {
                        size += 1
                        buffer.writeStringArg("FORCE", charset)
                    }
                }
            }
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(charset: Charset, vararg option: FailoverOptions): CommandRequest = encode(
        charset,
        option = option,
    )

    public suspend fun decode(input: Buffer, charset: Charset): Boolean {
        val code = input.parseCode(RespCode.SIMPLE_STRING)
        return when (code) {
            RespCode.SIMPLE_STRING -> {
                SimpleStringDecoder.decode(input, charset, code) == "OK"
            }

            else -> {
                throw UnexpectedResponseType("Expected [SIMPLE_STRING] but got $code", input.tryInferCause(code))
            }
        }
    }
}
