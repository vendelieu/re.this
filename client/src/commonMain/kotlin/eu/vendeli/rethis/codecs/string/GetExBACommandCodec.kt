package eu.vendeli.rethis.codecs.string

import eu.vendeli.rethis.shared.decoders.general.BulkByteArrayDecoder
import eu.vendeli.rethis.shared.request.string.GetExOption
import eu.vendeli.rethis.shared.types.*
import eu.vendeli.rethis.shared.utils.CRC16
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.shared.utils.validateSlot
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeDurationArg
import eu.vendeli.rethis.utils.writeInstantArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object GetExBACommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$5\r\nGETEX\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        vararg expiration: GetExOption,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(key, charset)
        expiration.forEach { it0 ->
            when (it0) {
                is GetExOption.Ex -> {
                    size += 1
                    buffer.writeStringArg("EX", charset)
                    size += 1
                    buffer.writeDurationArg(it0.seconds, charset, TimeUnit.SECONDS)
                }

                is GetExOption.ExAt -> {
                    size += 1
                    buffer.writeStringArg("EXAT", charset)
                    size += 1
                    buffer.writeInstantArg(it0.unixTimeSeconds, charset, TimeUnit.SECONDS)
                }

                is GetExOption.Persist -> {
                    size += 1
                    buffer.writeStringArg("PERSIST", charset)
                }

                is GetExOption.Px -> {
                    size += 1
                    buffer.writeStringArg("PX", charset)
                    size += 1
                    buffer.writeDurationArg(it0.milliseconds, charset, TimeUnit.MILLISECONDS)
                }

                is GetExOption.PxAt -> {
                    size += 1
                    buffer.writeStringArg("PXAT", charset)
                    size += 1
                    buffer.writeInstantArg(it0.unixTimeMilliseconds, charset, TimeUnit.MILLISECONDS)
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
        vararg expiration: GetExOption,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key.toByteArray(charset)))
        val request = encode(charset, key = key, expiration = expiration)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): ByteArray? {
        val code = input.parseCode(RespCode.BULK)
        return when (code) {
            RespCode.BULK -> {
                BulkByteArrayDecoder.decodeNullable(input, charset, code)
            }

            RespCode.NULL -> {
                null
            }

            else -> {
                throw UnexpectedResponseType("Expected [BULK, NULL] but got $code", input.tryInferCause(code))
            }
        }
    }
}
