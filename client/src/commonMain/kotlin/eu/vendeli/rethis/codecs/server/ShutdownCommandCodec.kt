package eu.vendeli.rethis.codecs.server

import eu.vendeli.rethis.api.spec.common.decoders.general.SimpleStringDecoder
import eu.vendeli.rethis.api.spec.common.request.server.SaveSelector
import eu.vendeli.rethis.api.spec.common.request.server.ShutdownOptions
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object ShutdownCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$8\r\nSHUTDOWN\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        saveSelector: SaveSelector?,
        vararg options: ShutdownOptions,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        saveSelector?.let { it0 ->
            when (it0) {
                is SaveSelector.NOSAVE ->  {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }
                is SaveSelector.SAVE ->  {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }
            }
        }
        options.forEach { it1 ->
            when (it1) {
                is ShutdownOptions.ABORT ->  {
                    size += 1
                    buffer.writeStringArg(it1.toString(), charset)
                }
                is ShutdownOptions.FORCE ->  {
                    size += 1
                    buffer.writeStringArg(it1.toString(), charset)
                }
                is ShutdownOptions.NOW ->  {
                    size += 1
                    buffer.writeStringArg(it1.toString(), charset)
                }
            }
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        saveSelector: SaveSelector?,
        vararg options: ShutdownOptions,
    ): CommandRequest = encode(charset, saveSelector = saveSelector, options = options)

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
