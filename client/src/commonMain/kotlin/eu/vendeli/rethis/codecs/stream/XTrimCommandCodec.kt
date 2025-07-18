package eu.vendeli.rethis.codecs.stream

import eu.vendeli.rethis.api.spec.common.decoders.general.IntegerDecoder
import eu.vendeli.rethis.api.spec.common.request.stream.Approximate
import eu.vendeli.rethis.api.spec.common.request.stream.Equal
import eu.vendeli.rethis.api.spec.common.request.stream.Exactement
import eu.vendeli.rethis.api.spec.common.request.stream.MAXLEN
import eu.vendeli.rethis.api.spec.common.request.stream.MINID
import eu.vendeli.rethis.api.spec.common.request.stream.TrimmingStrategy
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

public object XTrimCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$5\r\nXTRIM\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        threshold: String,
        strategy: TrimmingStrategy,
        `operator`: Exactement?,
        count: Long?,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 0
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(key, charset, )
        when (strategy) {
            is MAXLEN ->  {
                size += 1
                buffer.writeStringArg(strategy.toString(), charset)
            }
            is MINID ->  {
                size += 1
                buffer.writeStringArg(strategy.toString(), charset)
            }
        }
        operator?.let { it0 ->
            when (it0) {
                is Approximate ->  {
                    size += 1
                    buffer.writeStringArg("~", charset)
                }
                is Equal ->  {
                    size += 1
                    buffer.writeStringArg("=", charset)
                }
            }
        }
        size += 1
        buffer.writeStringArg(threshold, charset, )
        count?.let { it1 ->
            size += 1
            buffer.writeStringArg("LIMIT", charset)
            size += 1
            buffer.writeLongArg(it1, charset, )
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
        threshold: String,
        strategy: TrimmingStrategy,
        `operator`: Exactement?,
        count: Long?,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key.toByteArray(charset)))
        val request = encode(charset, key = key, threshold = threshold, strategy = strategy, operator = operator, count = count)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): Long {
        val code = RespCode.fromCode(input.readByte())
        return when(code) {
            RespCode.INTEGER -> {
                IntegerDecoder.decode(input, charset)
            }
            else -> {
                throw UnexpectedResponseType("Expected [INTEGER] but got $code", input.tryInferCause(code))
            }
        }
    }
}
