package eu.vendeli.rethis.codecs.stream

import eu.vendeli.rethis.api.spec.common.decoders.aggregate.ArrayRTypeDecoder
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.api.spec.common.utils.validateSlot
import eu.vendeli.rethis.utils.parseCode
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

public object XAutoClaimCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$10\r\nXAUTOCLAIM\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        group: String,
        consumer: String,
        minIdleTime: String,
        start: String,
        count: Long?,
        justid: Boolean?,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(key, charset, )
        size += 1
        buffer.writeStringArg(group, charset, )
        size += 1
        buffer.writeStringArg(consumer, charset, )
        size += 1
        buffer.writeStringArg(minIdleTime, charset, )
        size += 1
        buffer.writeStringArg(start, charset, )
        count?.let { it0 ->
            size += 1
            buffer.writeStringArg("COUNT", charset)
            size += 1
            buffer.writeLongArg(it0, charset, )
        }
        justid?.let { it1 ->
            if(it1) {
                size += 1
                buffer.writeStringArg("JUSTID", charset)
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
        key: String,
        group: String,
        consumer: String,
        minIdleTime: String,
        start: String,
        count: Long?,
        justid: Boolean?,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key.toByteArray(charset)))
        val request = encode(charset, key = key, group = group, consumer = consumer, minIdleTime = minIdleTime, start = start, count = count, justid = justid)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): List<RType> {
        val code = input.parseCode(RespCode.ARRAY)
        return when(code) {
            RespCode.ARRAY -> {
                ArrayRTypeDecoder.decode(input, charset, code)
            }
            else -> {
                throw UnexpectedResponseType("Expected [ARRAY] but got $code", input.tryInferCause(code))
            }
        }
    }
}
