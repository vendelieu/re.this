package eu.vendeli.rethis.codecs.string

import eu.vendeli.rethis.api.spec.common.decoders.general.IntegerDecoder
import eu.vendeli.rethis.api.spec.common.request.string.KeyValue
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.KeyAbsentException
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.api.spec.common.utils.validateSlot
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object MSetNxCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$6\r\nMSETNX\r\n")
    }

    public suspend fun encode(charset: Charset, vararg `data`: KeyValue): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        data.forEach { it0 ->
            size += 1
            buffer.writeStringArg(it0.key, charset, )
            size += 1
            buffer.writeStringArg(it0.value, charset, )
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(charset: Charset, vararg `data`: KeyValue): CommandRequest {
        var slot: Int? = null
        data.forEach { it0 ->
            slot = validateSlot(slot, CRC16.lookup(it0.key.toByteArray(charset)))
        }
        if(slot == null) throw KeyAbsentException("Expected key is not provided")
        val request = encode(charset, data = data)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): Boolean {
        val code = RespCode.fromCode(input.readByte())
        return when(code) {
            RespCode.INTEGER -> {
                IntegerDecoder.decode(input, charset) == 1L
            }
            else -> {
                throw UnexpectedResponseType("Expected [INTEGER] but got $code", input.tryInferCause(code))
            }
        }
    }
}
