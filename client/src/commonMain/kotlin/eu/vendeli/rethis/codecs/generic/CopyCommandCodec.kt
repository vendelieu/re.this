package eu.vendeli.rethis.codecs.generic

import eu.vendeli.rethis.shared.decoders.general.IntegerDecoder
import eu.vendeli.rethis.shared.request.generic.CopyOption
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.UnexpectedResponseType
import eu.vendeli.rethis.shared.utils.CRC16
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.shared.utils.validateSlot
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object CopyCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$4\r\nCOPY\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        source: String,
        destination: String,
        vararg option: CopyOption,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(source, charset)
        size += 1
        buffer.writeStringArg(destination, charset)
        option.forEach { it0 ->
            when (it0) {
                is CopyOption.DB -> {
                    size += 1
                    buffer.writeStringArg("DB", charset)
                    size += 1
                    buffer.writeLongArg(it0.destinationDb, charset)
                }

                is CopyOption.REPLACE -> {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
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
        source: String,
        destination: String,
        vararg option: CopyOption,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(source.toByteArray(charset)))
        slot = validateSlot(slot, CRC16.lookup(destination.toByteArray(charset)))
        val request = encode(charset, source = source, destination = destination, option = option)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): Boolean {
        val code = input.parseCode(RespCode.INTEGER)
        return when (code) {
            RespCode.INTEGER -> {
                IntegerDecoder.decode(input, charset, code) == 1L
            }

            else -> {
                throw UnexpectedResponseType("Expected [INTEGER] but got $code", input.tryInferCause(code))
            }
        }
    }
}
