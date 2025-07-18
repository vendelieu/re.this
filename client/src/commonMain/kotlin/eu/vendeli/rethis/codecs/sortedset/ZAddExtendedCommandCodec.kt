package eu.vendeli.rethis.codecs.sortedset

import eu.vendeli.rethis.api.spec.common.decoders.general.BulkStringDecoder
import eu.vendeli.rethis.api.spec.common.decoders.general.DoubleDecoder
import eu.vendeli.rethis.api.spec.common.request.common.UpdateStrategyOption
import eu.vendeli.rethis.api.spec.common.request.common.UpdateStrategyOption.GT
import eu.vendeli.rethis.api.spec.common.request.common.UpdateStrategyOption.LT
import eu.vendeli.rethis.api.spec.common.request.common.UpdateStrategyOption.NX
import eu.vendeli.rethis.api.spec.common.request.common.UpdateStrategyOption.XX
import eu.vendeli.rethis.api.spec.common.response.ZMember
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
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

public object ZAddExtendedCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$4\r\nZADD\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        vararg `data`: ZMember,
        condition: UpdateStrategyOption.ExistenceRule?,
        comparison: UpdateStrategyOption.ComparisonRule?,
        change: Boolean?,
        increment: Boolean?,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 0
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(key, charset, )
        condition?.let { it0 ->
            when (it0) {
                is UpdateStrategyOption.NX ->  {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }
                is UpdateStrategyOption.XX ->  {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }
            }
        }
        comparison?.let { it1 ->
            when (it1) {
                is UpdateStrategyOption.GT ->  {
                    size += 1
                    buffer.writeStringArg(it1.toString(), charset)
                }
                is UpdateStrategyOption.LT ->  {
                    size += 1
                    buffer.writeStringArg(it1.toString(), charset)
                }
            }
        }
        change?.let { it2 ->
            if(it2) {
                size += 1
                buffer.writeStringArg("CH", charset)
            }
        }
        increment?.let { it3 ->
            if(it3) {
                size += 1
                buffer.writeStringArg("INCR", charset)
            }
        }
        data.forEach { it4 ->
            size += 1
            buffer.writeDoubleArg(it4.score, charset, )
            size += 1
            buffer.writeStringArg(it4.member, charset, )
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        key: String,
        vararg `data`: ZMember,
        condition: UpdateStrategyOption.ExistenceRule?,
        comparison: UpdateStrategyOption.ComparisonRule?,
        change: Boolean?,
        increment: Boolean?,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key.toByteArray(charset)))
        val request = encode(charset, key = key, data = data, condition = condition, comparison = comparison, change = change, increment = increment)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): Double? {
        val code = RespCode.fromCode(input.readByte())
        return when(code) {
            RespCode.DOUBLE -> {
                DoubleDecoder.decode(input, charset)
            }
            RespCode.BULK -> {
                BulkStringDecoder.decode(input, charset).toDouble()
            }
            RespCode.NULL -> {
                null
            }
            else -> {
                throw UnexpectedResponseType("Expected [DOUBLE, BULK, NULL] but got $code", input.tryInferCause(code))
            }
        }
    }
}
