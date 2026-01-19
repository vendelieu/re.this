package eu.vendeli.rethis.codecs.sortedset

import eu.vendeli.rethis.shared.decoders.sortedset.ZPopResultDecoder
import eu.vendeli.rethis.shared.response.stream.ZPopResult
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.KeyAbsentException
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.utils.CRC16
import eu.vendeli.rethis.shared.utils.validateSlot
import eu.vendeli.rethis.utils.writeDoubleArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object BzPopMinCommandCodec {
    private const val BLOCKING_STATUS: Boolean = true

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$8\r\nBZPOPMIN\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        timeout: Double,
        vararg key: String,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        key.forEach { it0 ->
            size += 1
            buffer.writeStringArg(it0, charset)
        }
        size += 1
        buffer.writeDoubleArg(timeout, charset)

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
    ): CommandRequest {
        var slot: Int? = null
        key.forEach { it0 ->
            slot = validateSlot(slot, CRC16.lookup(it0.toByteArray(charset)))
        }
        if (slot == null) throw KeyAbsentException("Expected key is not provided")
        val request = encode(charset, timeout = timeout, key = key)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): ZPopResult? = ZPopResultDecoder.decode(input, charset)
}
