package eu.vendeli.rethis.codecs.scripting

import eu.vendeli.rethis.shared.decoders.common.ArrayIntBooleanDecoder
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object ScriptExistsCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$6\r\nSCRIPT\r\n$6\r\nEXISTS\r\n")
    }

    public suspend fun encode(charset: Charset, vararg sha1: String): CommandRequest {
        var buffer = Buffer()
        var size = 2
        COMMAND_HEADER.copyTo(buffer)
        sha1.forEach { it0 ->
            size += 1
            buffer.writeStringArg(it0, charset)
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(charset: Charset, vararg sha1: String): CommandRequest = encode(
        charset,
        sha1 = sha1,
    )

    public suspend fun decode(input: Buffer, charset: Charset): List<Boolean> = ArrayIntBooleanDecoder.decode(
        input,
        charset,
    )
}
