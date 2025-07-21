package eu.vendeli.rethis.codecs.server

import eu.vendeli.rethis.api.spec.common.decoders.aggregate.ArrayStringDecoder
import eu.vendeli.rethis.api.spec.common.decoders.general.SimpleErrorDecoder
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.String
import kotlin.collections.List
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object AclCatCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$3\r\nACL\r\n$3\r\nCAT\r\n")
    }

    public suspend fun encode(charset: Charset, category: String?): CommandRequest {
        var buffer = Buffer()
        var size = 2
        COMMAND_HEADER.copyTo(buffer)
        category?.let { it0 ->
            size += 1
            buffer.writeStringArg(it0, charset, )
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(charset: Charset, category: String?): CommandRequest = encode(charset, category = category)

    public suspend fun decode(input: Buffer, charset: Charset): List<String> {
        val code = RespCode.fromCode(input.readByte())
        return when(code) {
            RespCode.ARRAY -> {
                ArrayStringDecoder.decode(input, charset)
            }
            RespCode.SIMPLE_ERROR -> {
                SimpleErrorDecoder.decode(input, charset)
            }
            else -> {
                throw UnexpectedResponseType("Expected [ARRAY, SIMPLE_ERROR] but got $code", input.tryInferCause(code))
            }
        }
    }
}
