package eu.vendeli.rethis.codecs.sentinel

import eu.vendeli.rethis.api.spec.common.decoders.general.SimpleStringDecoder
import eu.vendeli.rethis.api.spec.common.request.common.FieldValue
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.String
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object SentinelSetCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$8\r\nSENTINEL\r\n$3\r\nSET\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        name: String,
        vararg optionValue: FieldValue,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 2
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(name, charset, )
        optionValue.forEach { it0 ->
            size += 1
            buffer.writeStringArg(it0.field, charset, )
            size += 1
            buffer.writeStringArg(it0.value, charset, )
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        name: String,
        vararg optionValue: FieldValue,
    ): CommandRequest = encode(charset, name = name, optionValue = optionValue)

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
