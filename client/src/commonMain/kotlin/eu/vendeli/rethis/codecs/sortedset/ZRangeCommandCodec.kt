package eu.vendeli.rethis.codecs.sortedset

import eu.vendeli.rethis.api.spec.common.decoders.aggregate.ArrayStringDecoder
import eu.vendeli.rethis.api.spec.common.request.sortedset.ZRangeOption
import eu.vendeli.rethis.api.spec.common.request.sortedset.ZRangeOption.BYLEX
import eu.vendeli.rethis.api.spec.common.request.sortedset.ZRangeOption.BYSCORE
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
import kotlin.String
import kotlin.collections.List
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object ZRangeCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$6\r\nZRANGE\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        start: String,
        stop: String,
        sortBy: ZRangeOption.Type?,
        rev: Boolean?,
        limit: ZRangeOption.Limit?,
        withScores: Boolean?,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 0
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(key, charset, )
        size += 1
        buffer.writeStringArg(start, charset, )
        size += 1
        buffer.writeStringArg(stop, charset, )
        sortBy?.let { it0 ->
            when (it0) {
                is ZRangeOption.BYLEX ->  {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }
                is ZRangeOption.BYSCORE ->  {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }
            }
        }
        rev?.let { it1 ->
            if(it1) {
                size += 1
                buffer.writeStringArg("REV", charset)
            }
        }
        limit?.let { it2 ->
            size += 1
            buffer.writeStringArg("LIMIT", charset)
            size += 1
            buffer.writeLongArg(it2.offset, charset, )
            size += 1
            buffer.writeLongArg(it2.count, charset, )
        }
        withScores?.let { it3 ->
            if(it3) {
                size += 1
                buffer.writeStringArg("WITHSCORES", charset)
            }
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
        start: String,
        stop: String,
        sortBy: ZRangeOption.Type?,
        rev: Boolean?,
        limit: ZRangeOption.Limit?,
        withScores: Boolean?,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key.toByteArray(charset)))
        val request = encode(charset, key = key, start = start, stop = stop, sortBy = sortBy, rev = rev, limit = limit, withScores = withScores)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): List<String> {
        val code = RespCode.fromCode(input.readByte())
        return when(code) {
            RespCode.ARRAY -> {
                ArrayStringDecoder.decode(input, charset)
            }
            else -> {
                throw UnexpectedResponseType("Expected [ARRAY] but got $code", input.tryInferCause(code))
            }
        }
    }
}
