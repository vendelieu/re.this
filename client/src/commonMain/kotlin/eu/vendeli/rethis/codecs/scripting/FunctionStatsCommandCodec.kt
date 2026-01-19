package eu.vendeli.rethis.codecs.scripting

import eu.vendeli.rethis.shared.decoders.aggregate.MapRTypeDecoder
import eu.vendeli.rethis.shared.types.*
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.utils.parseCode
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object FunctionStatsCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("*2\r\n$8\r\nFUNCTION\r\n$5\r\nSTATS\r\n")
    }

    public suspend fun encode(charset: Charset): CommandRequest {
        val buffer = Buffer()
        COMMAND_HEADER.copyTo(buffer)

        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(charset: Charset): CommandRequest = encode(charset)

    public suspend fun decode(input: Buffer, charset: Charset): Map<String, RType> {
        val code = input.parseCode(RespCode.MAP)
        return when (code) {
            RespCode.MAP -> {
                MapRTypeDecoder.decode(input, charset, code)
            }

            RespCode.ARRAY -> {
                MapRTypeDecoder.decode(input, charset, code)
            }

            else -> {
                throw UnexpectedResponseType("Expected [MAP, ARRAY] but got $code", input.tryInferCause(code))
            }
        }
    }
}
