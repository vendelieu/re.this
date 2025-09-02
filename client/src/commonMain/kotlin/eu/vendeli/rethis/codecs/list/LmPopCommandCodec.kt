package eu.vendeli.rethis.codecs.list

import eu.vendeli.rethis.shared.decoders.common.LMPopDecoder
import eu.vendeli.rethis.shared.response.common.MPopResult
import eu.vendeli.rethis.shared.response.common.MoveDirection
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.KeyAbsentException
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.utils.CRC16
import eu.vendeli.rethis.shared.utils.validateSlot
import eu.vendeli.rethis.utils.writeIntArg
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object LmPopCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$5\r\nLMPOP\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        `where`: MoveDirection,
        vararg key: String,
        count: Long?,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeIntArg(key.size, charset)
        key.forEach { it0 ->
            size += 1
            buffer.writeStringArg(it0, charset, )
        }
        size += 1
        buffer.writeStringArg(where.toString(), charset)
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
        `where`: MoveDirection,
        vararg key: String,
        count: Long?,
    ): CommandRequest {
        var slot: Int? = null
        key.forEach { it0 ->
            slot = validateSlot(slot, CRC16.lookup(it0.toByteArray(charset)))
        }
        if (slot == null) throw KeyAbsentException("Expected key is not provided")
        val request = encode(charset, where = where, key = key, count = count)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): List<MPopResult>? = LMPopDecoder.decode(input, charset)
}
