package eu.vendeli.rethis.codecs.server

import eu.vendeli.rethis.shared.decoders.connection.AclLogDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object AclLogCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$3\r\nACL\r\n$3\r\nLOG\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        count: Long?,
        reset: Boolean?,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 2
        COMMAND_HEADER.copyTo(buffer)
        count?.let { it0 ->
            size += 1
            buffer.writeLongArg(it0, charset, )
        }
        reset?.let { it1 ->
            if(it1) {
                size += 1
                buffer.writeStringArg("RESET", charset)
            }
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        count: Long?,
        reset: Boolean?,
    ): CommandRequest = encode(charset, count = count, reset = reset)

    public suspend fun decode(input: Buffer, charset: Charset): List<String> = AclLogDecoder.decode(input, charset)
}
