package eu.vendeli.rethis.codecs.list

import eu.vendeli.rethis.api.spec.common.decoders.general.BulkStringDecoder
import eu.vendeli.rethis.api.spec.common.response.common.MoveDirection
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.api.spec.common.utils.validateSlot
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeDoubleArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.Double
import kotlin.String
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object BlMoveCommandCodec {
    private const val BLOCKING_STATUS: Boolean = true

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("*6\r\n$6\r\nBLMOVE\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        source: String,
        destination: String,
        whereFrom: MoveDirection,
        whereTo: MoveDirection,
        timeout: Double,
    ): CommandRequest {
        val buffer = Buffer()
        COMMAND_HEADER.copyTo(buffer)
        buffer.writeStringArg(whereFrom.toString(), charset)
        buffer.writeStringArg(whereTo.toString(), charset)
        buffer.writeStringArg(source, charset, )
        buffer.writeStringArg(destination, charset, )
        buffer.writeDoubleArg(timeout, charset, )

        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        source: String,
        destination: String,
        whereFrom: MoveDirection,
        whereTo: MoveDirection,
        timeout: Double,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(source.toByteArray(charset)))
        slot = validateSlot(slot, CRC16.lookup(destination.toByteArray(charset)))
        val request = encode(charset, source = source, destination = destination, whereFrom = whereFrom, whereTo = whereTo, timeout = timeout)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): String? {
        val code = input.parseCode(RespCode.BULK)
        return when(code) {
            RespCode.BULK -> {
                BulkStringDecoder.decode(input, charset, code)
            }
            RespCode.NULL -> {
                null
            }
            else -> {
                throw UnexpectedResponseType("Expected [BULK, NULL] but got $code", input.tryInferCause(code))
            }
        }
    }
}
