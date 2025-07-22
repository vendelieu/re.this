package eu.vendeli.rethis.codecs.bitmap

import eu.vendeli.rethis.api.spec.common.decoders.aggregate.ArrayLongDecoder
import eu.vendeli.rethis.api.spec.common.request.bitmap.BitfieldOption
import eu.vendeli.rethis.api.spec.common.request.bitmap.BitfieldOption.Get
import eu.vendeli.rethis.api.spec.common.request.bitmap.BitfieldOption.IncreaseBy
import eu.vendeli.rethis.api.spec.common.request.bitmap.BitfieldOption.Overflow
import eu.vendeli.rethis.api.spec.common.request.bitmap.BitfieldOption.Set
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
import kotlin.collections.List
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object BitfieldCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$8\r\nBITFIELD\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        vararg operation: BitfieldOption,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        operation.forEach { it0 ->
            when (it0) {
                is BitfieldOption.Get ->  {
                    size += 1
                    buffer.writeStringArg("GET", charset)
                    size += 1
                    buffer.writeStringArg(it0.encoding, charset, )
                    size += 1
                    buffer.writeLongArg(it0.offset, charset, )
                }
                is BitfieldOption.IncreaseBy ->  {
                    size += 1
                    buffer.writeStringArg("INCRBY", charset)
                    size += 1
                    buffer.writeStringArg(it0.encoding, charset, )
                    size += 1
                    buffer.writeLongArg(it0.offset, charset, )
                    size += 1
                    buffer.writeLongArg(it0.increment, charset, )
                }
                is BitfieldOption.Overflow ->  {
                    size += 1
                    buffer.writeStringArg("OVERFLOW", charset)
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }
                is BitfieldOption.Set ->  {
                    size += 1
                    buffer.writeStringArg("SET", charset)
                    size += 1
                    buffer.writeStringArg(it0.encoding, charset, )
                    size += 1
                    buffer.writeLongArg(it0.offset, charset, )
                    size += 1
                    buffer.writeLongArg(it0.value, charset, )
                }
            }
        }
        size += 1
        buffer.writeStringArg(key, charset, )

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        key: String,
        vararg operation: BitfieldOption,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key.toByteArray(charset)))
        val request = encode(charset, key = key, operation = operation)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): List<Long>? {
        val code = RespCode.fromCode(input.readByte())
        return when(code) {
            RespCode.ARRAY -> {
                ArrayLongDecoder.decode(input, charset, code)
            }
            RespCode.NULL -> {
                null
            }
            else -> {
                throw UnexpectedResponseType("Expected [ARRAY, NULL] but got $code", input.tryInferCause(code))
            }
        }
    }
}
