package eu.vendeli.rethis.codecs.server

import eu.vendeli.rethis.shared.decoders.server.ShutdownDecoder
import eu.vendeli.rethis.shared.request.server.SaveSelector
import eu.vendeli.rethis.shared.request.server.ShutdownOptions
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object ShutdownCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$8\r\nSHUTDOWN\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        saveSelector: SaveSelector?,
        vararg options: ShutdownOptions,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        saveSelector?.let { it0 ->
            when (it0) {
                is SaveSelector.NOSAVE ->  {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }
                is SaveSelector.SAVE ->  {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }
            }
        }
        options.forEach { it1 ->
            when (it1) {
                is ShutdownOptions.ABORT ->  {
                    size += 1
                    buffer.writeStringArg(it1.toString(), charset)
                }
                is ShutdownOptions.FORCE ->  {
                    size += 1
                    buffer.writeStringArg(it1.toString(), charset)
                }
                is ShutdownOptions.NOW ->  {
                    size += 1
                    buffer.writeStringArg(it1.toString(), charset)
                }
            }
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        saveSelector: SaveSelector?,
        vararg options: ShutdownOptions,
    ): CommandRequest = encode(charset, saveSelector = saveSelector, options = options)

    public suspend fun decode(input: Buffer, charset: Charset): Boolean? = ShutdownDecoder.decode(input, charset)
}
