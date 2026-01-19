package eu.vendeli.rethis.codecs.sortedset

import eu.vendeli.rethis.shared.decoders.general.IntegerDecoder
import eu.vendeli.rethis.shared.request.sortedset.ZRangeOption
import eu.vendeli.rethis.shared.request.sortedset.ZRangeStoreLimit
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.UnexpectedResponseType
import eu.vendeli.rethis.shared.utils.CRC16
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.shared.utils.validateSlot
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object ZRangeStoreCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$11\r\nZRANGESTORE\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        dst: String,
        src: String,
        min: String,
        max: String,
        sortBy: ZRangeOption.Type?,
        rev: Boolean?,
        limit: ZRangeStoreLimit?,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(dst, charset)
        size += 1
        buffer.writeStringArg(src, charset)
        size += 1
        buffer.writeStringArg(min, charset)
        size += 1
        buffer.writeStringArg(max, charset)
        sortBy?.let { it0 ->
            when (it0) {
                is ZRangeOption.BYLEX -> {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }

                is ZRangeOption.BYSCORE -> {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }
            }
        }
        rev?.let { it1 ->
            if (it1) {
                size += 1
                buffer.writeStringArg("REV", charset)
            }
        }
        limit?.let { it2 ->
            size += 1
            buffer.writeStringArg("LIMIT", charset)
            size += 1
            buffer.writeLongArg(it2.offset, charset)
            size += 1
            buffer.writeLongArg(it2.count, charset)
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        dst: String,
        src: String,
        min: String,
        max: String,
        sortBy: ZRangeOption.Type?,
        rev: Boolean?,
        limit: ZRangeStoreLimit?,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(dst.toByteArray(charset)))
        slot = validateSlot(slot, CRC16.lookup(src.toByteArray(charset)))
        val request =
            encode(charset, dst = dst, src = src, min = min, max = max, sortBy = sortBy, rev = rev, limit = limit)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): Long {
        val code = input.parseCode(RespCode.INTEGER)
        return when (code) {
            RespCode.INTEGER -> {
                IntegerDecoder.decode(input, charset, code)
            }

            else -> {
                throw UnexpectedResponseType("Expected [INTEGER] but got $code", input.tryInferCause(code))
            }
        }
    }
}
