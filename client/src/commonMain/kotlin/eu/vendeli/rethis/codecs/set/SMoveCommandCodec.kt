package eu.vendeli.rethis.codecs.`set`

import eu.vendeli.rethis.api.spec.common.decoders.general.IntegerDecoder
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.api.spec.common.utils.validateSlot
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.String
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object SMoveCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("*4\r\n$5\r\nSMOVE\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        source: String,
        destination: String,
        member: String,
    ): CommandRequest {
        val buffer = Buffer()
        COMMAND_HEADER.copyTo(buffer)
        buffer.writeStringArg(source, charset, )
        buffer.writeStringArg(destination, charset, )
        buffer.writeStringArg(member, charset, )

        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        source: String,
        destination: String,
        member: String,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(source.toByteArray(charset)))
        slot = validateSlot(slot, CRC16.lookup(destination.toByteArray(charset)))
        val request = encode(charset, source = source, destination = destination, member = member)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): Boolean {
        val code = input.parseCode(RespCode.INTEGER)
        return when(code) {
            RespCode.INTEGER -> {
                IntegerDecoder.decode(input, charset, code) == 1L
            }
            else -> {
                throw UnexpectedResponseType("Expected [INTEGER] but got $code", input.tryInferCause(code))
            }
        }
    }
}
