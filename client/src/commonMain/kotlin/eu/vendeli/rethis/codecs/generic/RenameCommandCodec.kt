package eu.vendeli.rethis.codecs.generic

import eu.vendeli.rethis.shared.decoders.general.SimpleStringDecoder
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

public object RenameCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("*3\r\n$6\r\nRENAME\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        newkey: String,
    ): CommandRequest {
        val buffer = Buffer()
        COMMAND_HEADER.copyTo(buffer)
        buffer.writeStringArg(key, charset, )
        buffer.writeStringArg(newkey, charset, )

        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        key: String,
        newkey: String,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key.toByteArray(charset)))
        slot = validateSlot(slot, CRC16.lookup(newkey.toByteArray(charset)))
        val request = encode(charset, key = key, newkey = newkey)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): Boolean {
        val code = input.parseCode(RespCode.SIMPLE_STRING)
        return when(code) {
            RespCode.SIMPLE_STRING -> {
                SimpleStringDecoder.decode(input, charset, code) == "OK"
            }
            else -> {
                throw UnexpectedResponseType("Expected [SIMPLE_STRING] but got $code", input.tryInferCause(code))
            }
        }
    }
}
