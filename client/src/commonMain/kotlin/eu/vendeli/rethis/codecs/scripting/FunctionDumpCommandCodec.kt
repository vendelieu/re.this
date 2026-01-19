package eu.vendeli.rethis.codecs.scripting

import eu.vendeli.rethis.shared.decoders.generic.DumpDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisOperation
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object FunctionDumpCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("*2\r\n$8\r\nFUNCTION\r\n$4\r\nDUMP\r\n")
    }

    public suspend fun encode(charset: Charset): CommandRequest {
        val buffer = Buffer()
        COMMAND_HEADER.copyTo(buffer)

        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(charset: Charset): CommandRequest = encode(charset)

    public suspend fun decode(input: Buffer, charset: Charset): ByteArray? = DumpDecoder.decode(input, charset)
}
