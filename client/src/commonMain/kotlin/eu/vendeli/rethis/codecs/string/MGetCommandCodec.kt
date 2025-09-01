package eu.vendeli.rethis.codecs.string

import eu.vendeli.rethis.api.spec.common.decoders.aggregate.ArrayStringDecoder
import eu.vendeli.rethis.api.spec.common.types.*
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.api.spec.common.utils.validateSlot
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object MGetCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$4\r\nMGET\r\n")
    }

    public suspend fun encode(charset: Charset, vararg key: String): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        key.forEach { it0 ->
            size += 1
            buffer.writeStringArg(it0, charset, )
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(charset: Charset, vararg key: String): CommandRequest {
        var slot: Int? = null
        key.forEach { it0 ->
            slot = validateSlot(slot, CRC16.lookup(it0.toByteArray(charset)))
        }
        if (slot == null) throw KeyAbsentException("Expected key is not provided")
        val request = encode(charset, key = key)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): List<String?> {
        val code = input.parseCode(RespCode.ARRAY)
        return when(code) {
            RespCode.ARRAY -> {
                ArrayStringDecoder.decodeNullable(input, charset, code)
            }
            else -> {
                throw UnexpectedResponseType("Expected [ARRAY] but got $code", input.tryInferCause(code))
            }
        }
    }
}
