package eu.vendeli.rethis.codecs.list

import eu.vendeli.rethis.api.spec.common.decoders.general.IntegerDecoder
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.api.spec.common.utils.validateSlot
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object LPushxCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$6\r\nLPUSHX\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        vararg element: String,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(key, charset, )
        element.forEach { it0 ->
            size += 1
            buffer.writeStringArg(it0, charset, )
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        key: String,
        vararg element: String,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key.toByteArray(charset)))
        val request = encode(charset, key = key, element = element)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): Long {
        val code = input.parseCode(RespCode.INTEGER)
        return when(code) {
            RespCode.INTEGER -> {
                IntegerDecoder.decode(input, charset, code)
            }
            else -> {
                throw UnexpectedResponseType("Expected [INTEGER] but got $code", input.tryInferCause(code))
            }
        }
    }
}
