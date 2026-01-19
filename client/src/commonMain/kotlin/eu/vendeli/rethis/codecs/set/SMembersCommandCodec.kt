package eu.vendeli.rethis.codecs.`set`

import eu.vendeli.rethis.shared.decoders.aggregate.SetStringDecoder
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

public object SMembersCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("*2\r\n$8\r\nSMEMBERS\r\n")
    }

    public suspend fun encode(charset: Charset, key: String): CommandRequest {
        val buffer = Buffer()
        COMMAND_HEADER.copyTo(buffer)
        buffer.writeStringArg(key, charset)

        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(charset: Charset, key: String): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key.toByteArray(charset)))
        val request = encode(charset, key = key)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): Set<String> {
        val code = input.parseCode(RespCode.ARRAY)
        return when (code) {
            RespCode.ARRAY -> {
                SetStringDecoder.decode(input, charset, code)
            }

            RespCode.SET -> {
                SetStringDecoder.decode(input, charset, code)
            }

            else -> {
                throw UnexpectedResponseType("Expected [ARRAY, SET] but got $code", input.tryInferCause(code))
            }
        }
    }
}
