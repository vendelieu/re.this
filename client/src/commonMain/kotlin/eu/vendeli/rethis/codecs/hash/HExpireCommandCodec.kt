package eu.vendeli.rethis.codecs.hash

import eu.vendeli.rethis.shared.decoders.aggregate.ArrayLongDecoder
import eu.vendeli.rethis.shared.decoders.general.SimpleErrorDecoder
import eu.vendeli.rethis.shared.request.common.UpdateStrategyOption
import eu.vendeli.rethis.shared.types.*
import eu.vendeli.rethis.shared.utils.CRC16
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.shared.utils.validateSlot
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeDurationArg
import eu.vendeli.rethis.utils.writeIntArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.writeString
import kotlin.time.Duration

public object HExpireCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$7\r\nHEXPIRE\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        seconds: Duration,
        vararg `field`: String,
        condition: UpdateStrategyOption?,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(key, charset)
        size += 1
        buffer.writeDurationArg(seconds, charset, TimeUnit.SECONDS)
        condition?.let { it0 ->
            when (it0) {
                is UpdateStrategyOption.ComparisonRule -> {
                    when (it0) {
                        is UpdateStrategyOption.GT -> {
                            size += 1
                            buffer.writeStringArg(it0.toString(), charset)
                        }

                        is UpdateStrategyOption.LT -> {
                            size += 1
                            buffer.writeStringArg(it0.toString(), charset)
                        }
                    }
                }

                is UpdateStrategyOption.ExistenceRule -> {
                    when (it0) {
                        is UpdateStrategyOption.NX -> {
                            size += 1
                            buffer.writeStringArg(it0.toString(), charset)
                        }

                        is UpdateStrategyOption.XX -> {
                            size += 1
                            buffer.writeStringArg(it0.toString(), charset)
                        }
                    }
                }
            }
        }
        if (field.isNotEmpty()) {
            size += 1
            buffer.writeStringArg("FIELDS", charset)
            size += 1
            buffer.writeIntArg(field.size, charset)
        }
        field.forEach { it1 ->
            size += 1
            buffer.writeStringArg(it1, charset)
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
        seconds: Duration,
        vararg `field`: String,
        condition: UpdateStrategyOption?,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key.toByteArray(charset)))
        val request = encode(charset, key = key, seconds = seconds, field = field, condition = condition)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): List<Long> {
        val code = input.parseCode(RespCode.ARRAY)
        return when (code) {
            RespCode.ARRAY -> {
                ArrayLongDecoder.decode(input, charset, code)
            }

            RespCode.SIMPLE_ERROR -> {
                SimpleErrorDecoder.decode(input, charset, code)
            }

            else -> {
                throw UnexpectedResponseType("Expected [ARRAY, SIMPLE_ERROR] but got $code", input.tryInferCause(code))
            }
        }
    }
}
