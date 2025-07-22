package eu.vendeli.rethis.codecs.json

import eu.vendeli.rethis.api.spec.common.decoders.aggregate.ArrayRTypeDecoder
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.utils.parseCode
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

public object JsonArrPopCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$11\r\nJSON.ARRPOP\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        path: String?,
        index: Long?,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(key, charset, )
        path?.let { it0 ->
            size += 1
            buffer.writeStringArg(it0, charset, )
        }
        index?.let { it1 ->
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
        path: String?,
        index: Long?,
    ): CommandRequest = encode(charset, key = key, path = path, index = index)

    public suspend fun decode(input: Buffer, charset: Charset): List<RType> {
        val code = input.parseCode(RespCode.ARRAY)
        return when(code) {
            RespCode.ARRAY -> {
                ArrayRTypeDecoder.decode(input, charset, code)
            }
            else -> {
                throw UnexpectedResponseType("Expected [ARRAY] but got $code", input.tryInferCause(code))
            }
        }
    }
}
