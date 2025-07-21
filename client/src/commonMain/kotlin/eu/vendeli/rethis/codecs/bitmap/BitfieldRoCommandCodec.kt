package eu.vendeli.rethis.codecs.bitmap

import eu.vendeli.rethis.api.spec.common.decoders.aggregate.ArrayLongDecoder
import eu.vendeli.rethis.api.spec.common.request.bitmap.BitfieldOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.api.spec.common.utils.validateSlot
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object BitfieldRoCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$11\r\nBITFIELD_RO\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        vararg options: BitfieldOption.Get,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(key, charset, )
        options.forEach { it0 ->
            size += 1
            buffer.writeStringArg("GET", charset)
            size += 1
            buffer.writeStringArg(it0.encoding, charset, )
            size += 1
            buffer.writeLongArg(it0.offset, charset, )
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        key: String,
        vararg options: BitfieldOption.Get,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key.toByteArray(charset)))
        val request = encode(charset, key = key, options = options)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): List<Long> {
        val code = RespCode.fromCode(input.readByte())
        return when(code) {
            RespCode.ARRAY -> {
                ArrayLongDecoder.decode(input, charset)
            }
            else -> {
                throw UnexpectedResponseType("Expected [ARRAY] but got $code", input.tryInferCause(code))
            }
        }
    }
}
