package eu.vendeli.rethis.codecs.pubsub

import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object PSubscribeCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$10\r\nPSUBSCRIBE\r\n")
    }

    public suspend fun encode(charset: Charset, vararg pattern: String): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        pattern.forEach { it0 ->
            size += 1
            buffer.writeStringArg(it0, charset, )
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(charset: Charset, vararg pattern: String): CommandRequest = encode(charset, pattern = pattern)

    public suspend fun decode(input: Buffer, charset: Charset) {
    }
}
