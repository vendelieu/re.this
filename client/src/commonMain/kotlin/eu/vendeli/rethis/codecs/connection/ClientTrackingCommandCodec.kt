package eu.vendeli.rethis.codecs.connection

import eu.vendeli.rethis.api.spec.common.decoders.general.SimpleStringDecoder
import eu.vendeli.rethis.api.spec.common.request.connection.ClientStandby
import eu.vendeli.rethis.api.spec.common.request.connection.ClientStandby.OFF
import eu.vendeli.rethis.api.spec.common.request.connection.ClientStandby.ON
import eu.vendeli.rethis.api.spec.common.request.connection.ClientTrackingMode
import eu.vendeli.rethis.api.spec.common.request.connection.ClientTrackingMode.BROADCAST
import eu.vendeli.rethis.api.spec.common.request.connection.ClientTrackingMode.NOLOOP
import eu.vendeli.rethis.api.spec.common.request.connection.ClientTrackingMode.OPTIN
import eu.vendeli.rethis.api.spec.common.request.connection.ClientTrackingMode.OPTOUT
import eu.vendeli.rethis.api.spec.common.request.connection.ClientTrackingMode.Prefixes
import eu.vendeli.rethis.api.spec.common.request.connection.ClientTrackingMode.Redirect
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
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object ClientTrackingCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$6\r\nCLIENT\r\n$8\r\nTRACKING\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        status: ClientStandby,
        vararg options: ClientTrackingMode,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 2
        COMMAND_HEADER.copyTo(buffer)
        when (status) {
            is ClientStandby.OFF ->  {
                size += 1
                buffer.writeStringArg(status.toString(), charset)
            }
            is ClientStandby.ON ->  {
                size += 1
                buffer.writeStringArg(status.toString(), charset)
            }
        }
        options.forEach { it0 ->
            when (it0) {
                is ClientStandby ->  {
                    when (it0) {
                        is ClientStandby.ON ->  {
                            size += 1
                            buffer.writeStringArg(it0.toString(), charset)
                        }
                        is ClientStandby.OFF ->  {
                            size += 1
                            buffer.writeStringArg(it0.toString(), charset)
                        }
                    }
                }
                is ClientTrackingMode.BROADCAST ->  {
                    size += 1
                    buffer.writeStringArg("BCAST", charset)
                }
                is ClientTrackingMode.NOLOOP ->  {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }
                is ClientTrackingMode.OPTIN ->  {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }
                is ClientTrackingMode.OPTOUT ->  {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }
                is ClientTrackingMode.Prefixes ->  {
                    it0.prefix.forEach { it1 ->
                        size += 1
                        buffer.writeStringArg("PREFIX", charset)
                        size += 1
                        buffer.writeStringArg(it1, charset, )
                    }
                }
                is ClientTrackingMode.Redirect ->  {
                    size += 1
                    buffer.writeStringArg("REDIRECT", charset)
                    size += 1
                    buffer.writeLongArg(it0.clientId, charset, )
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
        status: ClientStandby,
        vararg options: ClientTrackingMode,
    ): CommandRequest = encode(charset, status = status, options = options)

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
