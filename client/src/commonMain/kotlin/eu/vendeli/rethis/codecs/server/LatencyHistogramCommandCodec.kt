package eu.vendeli.rethis.codecs.server

import eu.vendeli.rethis.shared.decoders.aggregate.MapRTypeDecoder
import eu.vendeli.rethis.shared.types.*
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object LatencyHistogramCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$7\r\nLATENCY\r\n$9\r\nHISTOGRAM\r\n")
    }

    public suspend fun encode(charset: Charset, vararg command: String): CommandRequest {
        var buffer = Buffer()
        var size = 2
        COMMAND_HEADER.copyTo(buffer)
        command.forEach { it0 ->
            size += 1
            buffer.writeStringArg(it0, charset)
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(charset: Charset, vararg command: String): CommandRequest = encode(
        charset,
        command = command,
    )

    public suspend fun decode(input: Buffer, charset: Charset): Map<String, RType> {
        val code = input.parseCode(RespCode.ARRAY)
        return when (code) {
            RespCode.ARRAY -> {
                MapRTypeDecoder.decode(input, charset, code)
            }

            RespCode.MAP -> {
                MapRTypeDecoder.decode(input, charset, code)
            }

            else -> {
                throw UnexpectedResponseType("Expected [ARRAY, MAP] but got $code", input.tryInferCause(code))
            }
        }
    }
}
