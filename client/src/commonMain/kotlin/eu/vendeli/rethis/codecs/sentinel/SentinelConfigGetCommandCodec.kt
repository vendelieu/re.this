package eu.vendeli.rethis.codecs.sentinel

import eu.vendeli.rethis.api.spec.common.decoders.aggregate.MapStringDecoder
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.String
import kotlin.collections.Map
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object SentinelConfigGetCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("*4\r\n$8\r\nSENTINEL\r\n$6\r\nCONFIG\r\n$3\r\nGET\r\n")
    }

    public suspend fun encode(charset: Charset, pattern: String): CommandRequest {
        val buffer = Buffer()
        COMMAND_HEADER.copyTo(buffer)
        buffer.writeStringArg(pattern, charset, )

        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(charset: Charset, pattern: String): CommandRequest = encode(charset, pattern = pattern)

    public suspend fun decode(input: Buffer, charset: Charset): Map<String, String> {
        val code = input.parseCode(RespCode.ARRAY)
        return when(code) {
            RespCode.ARRAY -> {
                MapStringDecoder.decode(input, charset, code)
            }
            RespCode.MAP -> {
                MapStringDecoder.decode(input, charset, code)
            }
            else -> {
                throw UnexpectedResponseType("Expected [ARRAY, MAP] but got $code", input.tryInferCause(code))
            }
        }
    }
}
