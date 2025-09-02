package eu.vendeli.rethis.codecs.json

import eu.vendeli.rethis.shared.decoders.general.RTypeDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.utils.writeDoubleArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object JsonNumIncrByCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("*4\r\n$14\r\nJSON.NUMINCRBY\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        path: String,
        `value`: Double,
    ): CommandRequest {
        val buffer = Buffer()
        COMMAND_HEADER.copyTo(buffer)
        buffer.writeStringArg(key, charset, )
        buffer.writeStringArg(path, charset, )
        buffer.writeDoubleArg(value, charset, )

        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        key: String,
        path: String,
        `value`: Double,
    ): CommandRequest = encode(charset, key = key, path = path, value = value)

    public suspend fun decode(input: Buffer, charset: Charset): RType = RTypeDecoder.decode(input, charset)
}
