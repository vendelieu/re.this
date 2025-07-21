package eu.vendeli.rethis.codecs.pubsub

import eu.vendeli.rethis.api.spec.common.decoders.pubsub.PubSubNumSubDecoder
import eu.vendeli.rethis.api.spec.common.response.common.PubSubNumEntry
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.String
import kotlin.collections.List
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object PubSubShardNumSubCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$6\r\nPUBSUB\r\n$11\r\nSHARDNUMSUB\r\n")
    }

    public suspend fun encode(charset: Charset, vararg shardchannel: String): CommandRequest {
        var buffer = Buffer()
        var size = 2
        COMMAND_HEADER.copyTo(buffer)
        shardchannel.forEach { it0 ->
            size += 1
            buffer.writeStringArg(it0, charset, )
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(charset: Charset, vararg shardchannel: String): CommandRequest = encode(charset, shardchannel = shardchannel)

    public suspend fun decode(input: Buffer, charset: Charset): List<PubSubNumEntry> = PubSubNumSubDecoder.decode(input, charset)
}
