package eu.vendeli.rethis.codecs.list

import eu.vendeli.rethis.api.spec.common.decoders.general.IntegerDecoder
import eu.vendeli.rethis.api.spec.common.request.list.LPosOption
import eu.vendeli.rethis.api.spec.common.request.list.LPosOption.MaxLen
import eu.vendeli.rethis.api.spec.common.request.list.LPosOption.Rank
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.api.spec.common.utils.validateSlot
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object LPosCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$4\r\nLPOS\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        element: String,
        vararg option: LPosOption,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 0
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(key, charset, )
        size += 1
        buffer.writeStringArg(element, charset, )
        option.forEach { it0 ->
            when (it0) {
                is LPosOption.MaxLen ->  {
                    size += 1
                    buffer.writeStringArg("MAXLEN", charset)
                    size += 1
                    buffer.writeLongArg(it0.len, charset, )
                }
                is LPosOption.Rank ->  {
                    size += 1
                    buffer.writeStringArg("RANK", charset)
                    size += 1
                    buffer.writeLongArg(it0.rank, charset, )
                }
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
        key: String,
        element: String,
        vararg option: LPosOption,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key.toByteArray(charset)))
        val request = encode(charset, key = key, element = element, option = option)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): Long? {
        val code = RespCode.fromCode(input.readByte())
        return when(code) {
            RespCode.INTEGER -> {
                IntegerDecoder.decode(input, charset)
            }
            RespCode.NULL -> {
                null
            }
            else -> {
                throw UnexpectedResponseType("Expected [INTEGER, NULL] but got $code", input.tryInferCause(code))
            }
        }
    }
}
