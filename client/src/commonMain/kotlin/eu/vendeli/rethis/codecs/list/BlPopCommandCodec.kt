package eu.vendeli.rethis.codecs.list

import eu.vendeli.rethis.api.spec.common.decoders.common.LPopDecoder
import eu.vendeli.rethis.api.spec.common.response.common.PopResult
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.KeyAbsentException
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.validateSlot
import eu.vendeli.rethis.utils.writeDoubleArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.Double
import kotlin.String
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object BlPopCommandCodec {
    private const val BLOCKING_STATUS: Boolean = true

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$5\r\nBLPOP\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        vararg key: String,
        timeout: Double,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 0
        COMMAND_HEADER.copyTo(buffer)
        key.forEach { it0 ->
            size += 1
            buffer.writeStringArg(it0, charset, )
        }
        size += 1
        buffer.writeDoubleArg(timeout, charset, )

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        vararg key: String,
        timeout: Double,
    ): CommandRequest {
        var slot: Int? = null
        key.forEach { it0 ->
            slot = validateSlot(slot, CRC16.lookup(it0.toByteArray(charset)))
        }
        if(slot == null) throw KeyAbsentException("Expected key is not provided")
        val request = encode(charset, key = key, timeout = timeout)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): PopResult? = LPopDecoder.decode(input, charset)
}
