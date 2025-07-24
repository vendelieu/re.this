package eu.vendeli.rethis.codecs.generic

import eu.vendeli.rethis.api.spec.common.decoders.general.SimpleStringDecoder
import eu.vendeli.rethis.api.spec.common.request.generic.RestoreOption
import eu.vendeli.rethis.api.spec.common.types.*
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.api.spec.common.utils.validateSlot
import eu.vendeli.rethis.utils.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object RestoreCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$7\r\nRESTORE\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        ttl: Long,
        serializedValue: ByteArray,
        vararg options: RestoreOption,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(key, charset, )
        size += 1
        buffer.writeLongArg(ttl, charset, )
        size += 1
        buffer.writeByteArrayArg(serializedValue, charset, )
        options.forEach { it0 ->
            when (it0) {
                is RestoreOption.ABSTTL ->  {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }
                is RestoreOption.Frequency ->  {
                    size += 1
                    buffer.writeStringArg("FREQ", charset)
                    size += 1
                    buffer.writeLongArg(it0.frequency, charset, )
                }
                is RestoreOption.IdleTime ->  {
                    size += 1
                    buffer.writeStringArg("IDLETIME", charset)
                    size += 1
                    buffer.writeDurationArg(it0.seconds, charset, TimeUnit.MILLISECONDS)
                }
                is RestoreOption.REPLACE ->  {
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
        key: String,
        ttl: Long,
        serializedValue: ByteArray,
        vararg options: RestoreOption,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key.toByteArray(charset)))
        val request = encode(charset, key = key, ttl = ttl, serializedValue = serializedValue, options = options)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): Boolean {
        val code = input.parseCode(RespCode.SIMPLE_STRING)
        return when(code) {
            RespCode.SIMPLE_STRING -> {
                SimpleStringDecoder.decode(input, charset, code) == "OK"
            }
            else -> {
                throw UnexpectedResponseType("Expected [SIMPLE_STRING] but got $code", input.tryInferCause(code))
            }
        }
    }
}
