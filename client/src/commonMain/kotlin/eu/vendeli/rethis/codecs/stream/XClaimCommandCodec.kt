package eu.vendeli.rethis.codecs.stream

import eu.vendeli.rethis.api.spec.common.decoders.aggregate.ArrayRTypeDecoder
import eu.vendeli.rethis.api.spec.common.request.stream.XClaimOption
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.TimeUnit
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.api.spec.common.utils.validateSlot
import eu.vendeli.rethis.utils.writeInstantArg
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.String
import kotlin.collections.List
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object XClaimCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$6\r\nXCLAIM\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        group: String,
        consumer: String,
        minIdleTime: String,
        vararg id: String,
        idle: XClaimOption.Idle?,
        time: XClaimOption.Time?,
        retryCount: XClaimOption.RetryCount?,
        force: Boolean?,
        justId: Boolean?,
        lastId: XClaimOption.LastId?,
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
        id.forEach { it0 ->
            size += 1
            buffer.writeStringArg(it0, charset, )
        }
        idle?.let { it1 ->
            size += 1
            buffer.writeStringArg("IDLE", charset)
            size += 1
            buffer.writeLongArg(it1.ms, charset, )
        }
        time?.let { it2 ->
            size += 1
            buffer.writeStringArg("TIME", charset)
            size += 1
            buffer.writeInstantArg(it2.unixTimeMilliseconds, charset, TimeUnit.MILLISECONDS)
        }
        retryCount?.let { it3 ->
            size += 1
            buffer.writeStringArg("RETRYCOUNT", charset)
            size += 1
            buffer.writeLongArg(it3.count, charset, )
        }
        force?.let { it4 ->
            if(it4) {
                size += 1
                buffer.writeStringArg("FORCE", charset)
            }
        }
        justId?.let { it5 ->
            if(it5) {
                size += 1
                buffer.writeStringArg("JUSTID", charset)
            }
        }
        lastId?.let { it6 ->
            size += 1
            buffer.writeStringArg("LASTID", charset)
            size += 1
            buffer.writeStringArg(it6.lastid, charset, )
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
        vararg id: String,
        idle: XClaimOption.Idle?,
        time: XClaimOption.Time?,
        retryCount: XClaimOption.RetryCount?,
        force: Boolean?,
        justId: Boolean?,
        lastId: XClaimOption.LastId?,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key.toByteArray(charset)))
        val request = encode(charset, key = key, group = group, consumer = consumer, minIdleTime = minIdleTime, id = id, idle = idle, time = time, retryCount = retryCount, force = force, justId = justId, lastId = lastId)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): List<RType> {
        val code = RespCode.fromCode(input.readByte())
        return when(code) {
            RespCode.ARRAY -> {
                ArrayRTypeDecoder.decode(input, charset)
            }
            else -> {
                throw UnexpectedResponseType("Expected [ARRAY] but got $code", input.tryInferCause(code))
            }
        }
    }
}
