package eu.vendeli.rethis.codecs.stream

import eu.vendeli.rethis.api.spec.common.decoders.aggregate.MapRTypeDecoder
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.KeyAbsentException
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.api.spec.common.utils.validateSlot
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object XReadCommandCodec {
    private const val BLOCKING_STATUS: Boolean = true

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$5\r\nXREAD\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: List<String>,
        id: List<String>,
        count: Long?,
        milliseconds: Long?,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        count?.let { it0 ->
            size += 1
            buffer.writeStringArg("COUNT", charset)
            size += 1
            buffer.writeLongArg(it0, charset, )
        }
        milliseconds?.let { it1 ->
            size += 1
            buffer.writeStringArg("BLOCK", charset)
            size += 1
            buffer.writeLongArg(it1, charset, )
        }
        key.forEach { it2 ->
            size += 1
            buffer.writeStringArg("STREAMS", charset)
            size += 1
            buffer.writeStringArg(it2, charset, )
        }
        id.forEach { it3 ->
            size += 1
            buffer.writeStringArg(it3, charset, )
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        key: List<String>,
        id: List<String>,
        count: Long?,
        milliseconds: Long?,
    ): CommandRequest {
        var slot: Int? = null
        key.forEach { it0 ->
            slot = validateSlot(slot, CRC16.lookup(it0.toByteArray(charset)))
        }
        if(slot == null) throw KeyAbsentException("Expected key is not provided")
        val request = encode(charset, key = key, id = id, count = count, milliseconds = milliseconds)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): Map<String, RType>? {
        val code = input.parseCode(RespCode.ARRAY)
        return when(code) {
            RespCode.ARRAY -> {
                MapRTypeDecoder.decode(input, charset, code)
            }
            RespCode.MAP -> {
                MapRTypeDecoder.decode(input, charset, code)
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
