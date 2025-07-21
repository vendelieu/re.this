package eu.vendeli.rethis.codecs.scripting

import eu.vendeli.rethis.api.spec.common.decoders.general.BulkStringDecoder
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.String
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object FunctionLoadCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$8\r\nFUNCTION\r\n$4\r\nLOAD\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        functionCode: String,
        replace: Boolean?,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 2
        COMMAND_HEADER.copyTo(buffer)
        replace?.let { it0 ->
            if(it0) {
                size += 1
                buffer.writeStringArg("REPLACE", charset)
            }
        }
        size += 1
        buffer.writeStringArg(functionCode, charset, )

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        functionCode: String,
        replace: Boolean?,
    ): CommandRequest = encode(charset, functionCode = functionCode, replace = replace)

    public suspend fun decode(input: Buffer, charset: Charset): String {
        val code = RespCode.fromCode(input.readByte())
        return when(code) {
            RespCode.BULK -> {
                BulkStringDecoder.decode(input, charset)
            }
            else -> {
                throw UnexpectedResponseType("Expected [BULK] but got $code", input.tryInferCause(code))
            }
        }
    }
}
