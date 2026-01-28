package eu.vendeli.rethis.codecs.stream

import eu.vendeli.rethis.shared.decoders.aggregate.ArrayRTypeDecoder
import eu.vendeli.rethis.shared.request.stream.XReadGroupKeyIds
import eu.vendeli.rethis.shared.request.stream.XReadGroupOption
import eu.vendeli.rethis.shared.types.*
import eu.vendeli.rethis.shared.utils.CRC16
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.shared.utils.validateSlot
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeDurationArg
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object XReadGroupCommandCodec {
    private const val BLOCKING_STATUS: Boolean = true

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$10\r\nXREADGROUP\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        group: String,
        consumer: String,
        streams: XReadGroupKeyIds,
        vararg option: XReadGroupOption,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg("GROUP", charset)
        size += 1
        buffer.writeStringArg(group, charset, )
        size += 1
        buffer.writeStringArg(consumer, charset, )
        option.forEach { it0 ->
            when (it0) {
                is XReadGroupOption.Block ->  {
                    size += 1
                    buffer.writeStringArg("BLOCK", charset)
                    size += 1
                    buffer.writeDurationArg(it0.milliseconds, charset, TimeUnit.MILLISECONDS)
                }
                is XReadGroupOption.Count ->  {
                    size += 1
                    buffer.writeStringArg("COUNT", charset)
                    size += 1
                    buffer.writeLongArg(it0.count, charset, )
                }
                is XReadGroupOption.NoAck ->  {
                    size += 1
                    buffer.writeStringArg("NOACK", charset)
                }
            }
        }
        size += 1
        buffer.writeStringArg("STREAMS", charset)
        streams.key.forEach { it1 ->
            size += 1
            buffer.writeStringArg(it1, charset, )
        }
        streams.id.forEach { it2 ->
            size += 1
            buffer.writeStringArg(it2, charset, )
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        group: String,
        consumer: String,
        streams: XReadGroupKeyIds,
        vararg option: XReadGroupOption,
    ): CommandRequest {
        var slot: Int? = null
        streams.key.forEach { it0 ->
            slot = validateSlot(slot, CRC16.lookup(it0.toByteArray(charset)))
        }
        if (slot == null) throw KeyAbsentException("Expected key is not provided")
        val request = encode(charset, group = group, consumer = consumer, streams = streams, option = option)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): List<RType>? {
        return when(val code = input.parseCode(RespCode.ARRAY)) {
            RespCode.ARRAY -> {
                ArrayRTypeDecoder.decode(input, charset, code)
            }
            RespCode.NULL -> {
                null
            }
            else -> {
                throw UnexpectedResponseType("Expected [ARRAY, MAP, NULL] but got $code", input.tryInferCause(code))
            }
        }
    }
}
