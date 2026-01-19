package eu.vendeli.rethis.codecs.pubsub

import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object SSubscribeCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$10\r\nSSUBSCRIBE\r\n")
    }

    public suspend fun encode(charset: Charset, vararg shardChannel: String): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        shardChannel.forEach { it0 ->
            size += 1
            buffer.writeStringArg(it0, charset)
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(charset: Charset, vararg shardChannel: String): CommandRequest = encode(
        charset,
        shardChannel = shardChannel,
    )

    public suspend fun decode(input: Buffer, charset: Charset) {
    }
}
