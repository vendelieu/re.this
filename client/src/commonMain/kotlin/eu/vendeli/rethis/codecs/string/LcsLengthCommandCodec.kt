package eu.vendeli.rethis.codecs.string

import eu.vendeli.rethis.shared.decoders.general.IntegerDecoder
import eu.vendeli.rethis.shared.request.string.LcsMode
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.UnexpectedResponseType
import eu.vendeli.rethis.shared.utils.CRC16
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.shared.utils.validateSlot
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object LcsLengthCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("*4\r\n$3\r\nLCS\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key1: String,
        key2: String,
        mode: LcsMode.LEN,
    ): CommandRequest {
        val buffer = Buffer()
        COMMAND_HEADER.copyTo(buffer)
        buffer.writeStringArg(key1, charset)
        buffer.writeStringArg(key2, charset)
        buffer.writeStringArg(mode.toString(), charset)

        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        key1: String,
        key2: String,
        mode: LcsMode.LEN,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key1.toByteArray(charset)))
        slot = validateSlot(slot, CRC16.lookup(key2.toByteArray(charset)))
        val request = encode(charset, key1 = key1, key2 = key2, mode = mode)
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
