package eu.vendeli.rethis.codecs.server

import eu.vendeli.rethis.shared.decoders.general.BulkStringDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.UnexpectedResponseType
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeLongArg
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object AclGenPassCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$3\r\nACL\r\n$7\r\nGENPASS\r\n")
    }

    public suspend fun encode(charset: Charset, bits: Long?): CommandRequest {
        var buffer = Buffer()
        var size = 2
        COMMAND_HEADER.copyTo(buffer)
        bits?.let { it0 ->
            size += 1
            buffer.writeLongArg(it0, charset)
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(charset: Charset, bits: Long?): CommandRequest = encode(
        charset,
        bits = bits,
    )

    public suspend fun decode(input: Buffer, charset: Charset): String {
        val code = input.parseCode(RespCode.BULK)
        return when (code) {
            RespCode.BULK -> {
                BulkStringDecoder.decode(input, charset, code)
            }

            else -> {
                throw UnexpectedResponseType("Expected [BULK] but got $code", input.tryInferCause(code))
            }
        }
    }
}
