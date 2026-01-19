package eu.vendeli.rethis.codecs.sentinel

import eu.vendeli.rethis.shared.decoders.aggregate.MapStringDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.UnexpectedResponseType
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.utils.parseCode
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object SentinelInfoCacheCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("*2\r\n$8\r\nSENTINEL\r\n$10\r\nINFO-CACHE\r\n")
    }

    public suspend fun encode(charset: Charset): CommandRequest {
        val buffer = Buffer()
        COMMAND_HEADER.copyTo(buffer)

        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(charset: Charset): CommandRequest = encode(charset)

    public suspend fun decode(input: Buffer, charset: Charset): Map<String, String> {
        val code = input.parseCode(RespCode.MAP)
        return when (code) {
            RespCode.MAP -> {
                MapStringDecoder.decode(input, charset, code)
            }

            else -> {
                throw UnexpectedResponseType("Expected [MAP] but got $code", input.tryInferCause(code))
            }
        }
    }
}
