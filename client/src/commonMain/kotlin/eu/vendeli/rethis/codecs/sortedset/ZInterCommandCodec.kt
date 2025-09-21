package eu.vendeli.rethis.codecs.sortedset

import eu.vendeli.rethis.shared.decoders.aggregate.ArrayStringDecoder
import eu.vendeli.rethis.shared.request.sortedset.ZAggregate
import eu.vendeli.rethis.shared.types.*
import eu.vendeli.rethis.shared.utils.CRC16
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.shared.utils.validateSlot
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeIntArg
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object ZInterCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$6\r\nZINTER\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        vararg key: String,
        weight: List<Long>?,
        aggregate: ZAggregate?,
        withScores: Boolean?,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeIntArg(key.size, charset)
        key.forEach { it0 ->
            size += 1
            buffer.writeStringArg(it0, charset, )
        }
        weight?.let { it1 ->
            if (it1.isNotEmpty()) {
                size += 1
                buffer.writeStringArg("WEIGHTS", charset)
            }
            it1.forEach { it2 ->
                size += 1
                buffer.writeLongArg(it2, charset, )
            }
        }
        aggregate?.let { it3 ->
            size += 1
            buffer.writeStringArg(it3.toString(), charset)
        }
        withScores?.let { it4 ->
            if(it4) {
                size += 1
                buffer.writeStringArg("WITHSCORES", charset)
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
        vararg key: String,
        weight: List<Long>?,
        aggregate: ZAggregate?,
        withScores: Boolean?,
    ): CommandRequest {
        var slot: Int? = null
        key.forEach { it0 ->
            slot = validateSlot(slot, CRC16.lookup(it0.toByteArray(charset)))
        }
        if (slot == null) throw KeyAbsentException("Expected key is not provided")
        val request = encode(charset, key = key, weight = weight, aggregate = aggregate, withScores = withScores)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): List<String> {
        val code = input.parseCode(RespCode.ARRAY)
        return when(code) {
            RespCode.ARRAY -> {
                ArrayStringDecoder.decode(input, charset, code)
            }
            else -> {
                throw UnexpectedResponseType("Expected [ARRAY] but got $code", input.tryInferCause(code))
            }
        }
    }
}
