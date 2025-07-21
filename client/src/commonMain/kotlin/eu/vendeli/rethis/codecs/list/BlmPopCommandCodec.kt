package eu.vendeli.rethis.codecs.list

import eu.vendeli.rethis.api.spec.common.decoders.common.LMPopDecoder
import eu.vendeli.rethis.api.spec.common.response.common.MPopResult
import eu.vendeli.rethis.api.spec.common.response.common.MoveDirection
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.KeyAbsentException
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.validateSlot
import eu.vendeli.rethis.utils.writeDoubleArg
import eu.vendeli.rethis.utils.writeIntArg
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.Double
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object BlmPopCommandCodec {
    private const val BLOCKING_STATUS: Boolean = true

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$6\r\nBLMPOP\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        timeout: Double,
        vararg key: String,
        `where`: MoveDirection,
        count: Long?,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(where.toString(), charset)
        size += 1
        buffer.writeDoubleArg(timeout, charset, )
        buffer.writeIntArg(key.size, charset)
        key.forEach { it0 ->
            size += 1
            buffer.writeStringArg(it0, charset, )
        }
        count?.let { it1 ->
            size += 1
            buffer.writeStringArg("COUNT", charset)
            size += 1
            buffer.writeLongArg(it1, charset, )
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        timeout: Double,
        vararg key: String,
        `where`: MoveDirection,
        count: Long?,
    ): CommandRequest {
        var slot: Int? = null
        key.forEach { it0 ->
            slot = validateSlot(slot, CRC16.lookup(it0.toByteArray(charset)))
        }
        if(slot == null) throw KeyAbsentException("Expected key is not provided")
        val request = encode(charset, timeout = timeout, key = key, where = where, count = count)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): List<MPopResult>? = LMPopDecoder.decode(input, charset)
}
