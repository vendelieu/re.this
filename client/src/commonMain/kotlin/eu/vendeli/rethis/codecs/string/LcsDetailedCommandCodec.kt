package eu.vendeli.rethis.codecs.string

import eu.vendeli.rethis.shared.decoders.string.LcsDecoder
import eu.vendeli.rethis.shared.request.string.LcsMode
import eu.vendeli.rethis.shared.request.string.MinMatchLen
import eu.vendeli.rethis.shared.response.string.LcsResult
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.utils.CRC16
import eu.vendeli.rethis.shared.utils.validateSlot
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object LcsDetailedCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$3\r\nLCS\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key1: String,
        key2: String,
        mode: LcsMode.IDX,
        minMatchLen: MinMatchLen?,
        withMatchLen: Boolean?,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(key1, charset, )
        size += 1
        buffer.writeStringArg(key2, charset, )
        size += 1
        buffer.writeStringArg(mode.toString(), charset)
        minMatchLen?.let { it0 ->
            size += 1
            buffer.writeStringArg("MINMATCHLEN", charset)
            size += 1
            buffer.writeLongArg(it0.minMatchLen, charset, )
        }
        withMatchLen?.let { it1 ->
            if(it1) {
                size += 1
                buffer.writeStringArg("WITHMATCHLEN", charset)
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
        key1: String,
        key2: String,
        mode: LcsMode.IDX,
        minMatchLen: MinMatchLen?,
        withMatchLen: Boolean?,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key1.toByteArray(charset)))
        slot = validateSlot(slot, CRC16.lookup(key2.toByteArray(charset)))
        val request = encode(charset, key1 = key1, key2 = key2, mode = mode, minMatchLen = minMatchLen, withMatchLen = withMatchLen)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): LcsResult = LcsDecoder.decode(input, charset)
}
