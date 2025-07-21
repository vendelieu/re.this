package eu.vendeli.rethis.codecs.generic

import eu.vendeli.rethis.api.spec.common.decoders.general.IntegerDecoder
import eu.vendeli.rethis.api.spec.common.request.common.UpdateStrategyOption
import eu.vendeli.rethis.api.spec.common.request.common.UpdateStrategyOption.ComparisonRule
import eu.vendeli.rethis.api.spec.common.request.common.UpdateStrategyOption.ExistenceRule
import eu.vendeli.rethis.api.spec.common.request.common.UpdateStrategyOption.GT
import eu.vendeli.rethis.api.spec.common.request.common.UpdateStrategyOption.LT
import eu.vendeli.rethis.api.spec.common.request.common.UpdateStrategyOption.NX
import eu.vendeli.rethis.api.spec.common.request.common.UpdateStrategyOption.XX
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.TimeUnit
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.api.spec.common.utils.validateSlot
import eu.vendeli.rethis.utils.writeInstantArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.String
import kotlin.time.Instant
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object PExpireAtCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$9\r\nPEXPIREAT\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        unixTimeMilliseconds: Instant,
        condition: UpdateStrategyOption?,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(key, charset, )
        size += 1
        buffer.writeInstantArg(unixTimeMilliseconds, charset, TimeUnit.MILLISECONDS)
        condition?.let { it0 ->
            when (it0) {
                is UpdateStrategyOption.ComparisonRule ->  {
                    when (it0) {
                        is UpdateStrategyOption.GT ->  {
                            size += 1
                            buffer.writeStringArg(it0.toString(), charset)
                        }
                        is UpdateStrategyOption.LT ->  {
                            size += 1
                            buffer.writeStringArg(it0.toString(), charset)
                        }
                    }
                }
                is UpdateStrategyOption.ExistenceRule ->  {
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
            }
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
        unixTimeMilliseconds: Instant,
        condition: UpdateStrategyOption?,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key.toByteArray(charset)))
        val request = encode(charset, key = key, unixTimeMilliseconds = unixTimeMilliseconds, condition = condition)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): Boolean {
        val code = RespCode.fromCode(input.readByte())
        return when(code) {
            RespCode.INTEGER -> {
                IntegerDecoder.decode(input, charset) == 1L
            }
            else -> {
                throw UnexpectedResponseType("Expected [INTEGER] but got $code", input.tryInferCause(code))
            }
        }
    }
}
