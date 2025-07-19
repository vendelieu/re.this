package eu.vendeli.rethis.codecs.pubsub

import eu.vendeli.rethis.api.spec.common.decoders.pubsub.PubSubNumSubDecoder
import eu.vendeli.rethis.api.spec.common.response.common.PubSubNumEntry
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import kotlin.Boolean
import kotlin.String
import kotlin.collections.List
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object PubSubNumSubCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$6\r\nPUBSUB\r\n$6\r\nNUMSUB\r\n")
    }

    public suspend fun encode(charset: Charset, vararg channel: String): CommandRequest {
        var buffer = Buffer()
        var size = 0
        COMMAND_HEADER.copyTo(buffer)
        channel.forEach { it0 ->
            size += 1
            buffer.writeStringArg(it0, charset, )
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(charset: Charset, vararg channel: String): CommandRequest = encode(charset, channel = channel)

    public suspend fun decode(input: Buffer, charset: Charset): List<PubSubNumEntry> = PubSubNumSubDecoder.decode(input, charset)
}
