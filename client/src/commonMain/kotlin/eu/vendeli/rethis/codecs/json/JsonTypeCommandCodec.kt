package eu.vendeli.rethis.codecs.json

import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object JsonTypeCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$9\r\nJSON.TYPE\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        path: String?,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(key, charset)
        path?.let { it0 ->
            size += 1
            buffer.writeStringArg(it0, charset)
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        key: String,
        path: String?,
    ): CommandRequest = encode(charset, key = key, path = path)

    public suspend fun decode(input: Buffer, charset: Charset): RType = RTypeDecoder.decode(input, charset)
}
