package eu.vendeli.rethis.codecs.connection

import eu.vendeli.rethis.api.spec.common.decoders.general.SimpleStringDecoder
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.utils.writeCharArrayArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.CharArray
import kotlin.String
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object AuthCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$4\r\nAUTH\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        username: String?,
        password: CharArray,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        username?.let { it0 ->
            size += 1
            buffer.writeStringArg(it0, charset, )
        }
        size += 1
        buffer.writeCharArrayArg(password, charset, )

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        username: String?,
        password: CharArray,
    ): CommandRequest = encode(charset, username = username, password = password)

    public suspend fun decode(input: Buffer, charset: Charset): Boolean {
        val code = RespCode.fromCode(input.readByte())
        return when(code) {
            RespCode.SIMPLE_STRING -> {
                SimpleStringDecoder.decode(input, charset) == "OK"
            }
            else -> {
                throw UnexpectedResponseType("Expected [SIMPLE_STRING] but got $code", input.tryInferCause(code))
            }
        }
    }
}
