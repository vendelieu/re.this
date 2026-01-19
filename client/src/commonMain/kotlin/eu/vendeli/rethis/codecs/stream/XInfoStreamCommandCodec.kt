package eu.vendeli.rethis.codecs.stream

import eu.vendeli.rethis.shared.decoders.aggregate.MapRTypeDecoder
import eu.vendeli.rethis.shared.types.*
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

public object XInfoStreamCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$5\r\nXINFO\r\n$6\r\nSTREAM\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        full: Boolean?,
        count: Long?,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 2
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(key, charset)
        full?.let { it0 ->
            if (it0) {
                size += 1
                buffer.writeStringArg("FULL", charset)
            }
        }
        count?.let { it1 ->
            size += 1
            buffer.writeStringArg("COUNT", charset)
            size += 1
            buffer.writeLongArg(it1, charset)
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
        full: Boolean?,
        count: Long?,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key.toByteArray(charset)))
        val request = encode(charset, key = key, full = full, count = count)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): Map<String, RType> {
        val code = input.parseCode(RespCode.MAP)
        return when (code) {
            RespCode.MAP -> {
                MapRTypeDecoder.decode(input, charset, code)
            }

            RespCode.ARRAY -> {
                MapRTypeDecoder.decode(input, charset, code)
            }

            else -> {
                throw UnexpectedResponseType("Expected [MAP, ARRAY] but got $code", input.tryInferCause(code))
            }
        }
    }
}
