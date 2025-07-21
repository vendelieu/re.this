package eu.vendeli.rethis.codecs.string

import eu.vendeli.rethis.api.spec.common.decoders.general.BulkStringDecoder
import eu.vendeli.rethis.api.spec.common.decoders.general.SimpleStringDecoder
import eu.vendeli.rethis.api.spec.common.request.string.GET
import eu.vendeli.rethis.api.spec.common.request.string.SetExpire
import eu.vendeli.rethis.api.spec.common.request.string.SetExpire.Ex
import eu.vendeli.rethis.api.spec.common.request.string.SetExpire.ExAt
import eu.vendeli.rethis.api.spec.common.request.string.SetExpire.KEEPTTL
import eu.vendeli.rethis.api.spec.common.request.string.SetExpire.Px
import eu.vendeli.rethis.api.spec.common.request.string.SetExpire.PxAt
import eu.vendeli.rethis.api.spec.common.request.string.SetOption
import eu.vendeli.rethis.api.spec.common.request.string.UpsertMode
import eu.vendeli.rethis.api.spec.common.request.string.UpsertMode.NX
import eu.vendeli.rethis.api.spec.common.request.string.UpsertMode.XX
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.TimeUnit
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.api.spec.common.utils.validateSlot
import eu.vendeli.rethis.utils.writeDurationArg
import eu.vendeli.rethis.utils.writeInstantArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.String
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object SetCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$3\r\nSET\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        `value`: String,
        vararg options: SetOption,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(key, charset, )
        size += 1
        buffer.writeStringArg(value, charset, )
        options.forEach { it0 ->
            when (it0) {
                is GET ->  {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }
                is SetExpire ->  {
                    when (it0) {
                        is SetExpire.Ex ->  {
                            size += 1
                            buffer.writeStringArg("EX", charset)
                            size += 1
                            buffer.writeDurationArg(it0.seconds, charset, TimeUnit.MILLISECONDS)
                        }
                        is SetExpire.Px ->  {
                            size += 1
                            buffer.writeStringArg("PX", charset)
                            size += 1
                            buffer.writeDurationArg(it0.milliseconds, charset, TimeUnit.MILLISECONDS)
                        }
                        is SetExpire.ExAt ->  {
                            size += 1
                            buffer.writeStringArg("EXAT", charset)
                            size += 1
                            buffer.writeInstantArg(it0.unixTimeSeconds, charset, TimeUnit.MILLISECONDS)
                        }
                        is SetExpire.PxAt ->  {
                            size += 1
                            buffer.writeStringArg("PXAT", charset)
                            size += 1
                            buffer.writeInstantArg(it0.unixTimeMilliseconds, charset, TimeUnit.MILLISECONDS)
                        }
                        is SetExpire.KEEPTTL ->  {
                            size += 1
                            buffer.writeStringArg(it0.toString(), charset)
                        }
                    }
                }
                is UpsertMode ->  {
                    when (it0) {
                        is UpsertMode.NX ->  {
                            size += 1
                            buffer.writeStringArg(it0.toString(), charset)
                        }
                        is UpsertMode.XX ->  {
                            size += 1
                            buffer.writeStringArg(it0.toString(), charset)
                        }
                    }
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
        `value`: String,
        vararg options: SetOption,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key.toByteArray(charset)))
        val request = encode(charset, key = key, value = value, options = options)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): String? {
        val code = RespCode.fromCode(input.readByte())
        return when(code) {
            RespCode.BULK -> {
                BulkStringDecoder.decode(input, charset)
            }
            RespCode.SIMPLE_STRING -> {
                SimpleStringDecoder.decode(input, charset)
            }
            RespCode.NULL -> {
                null
            }
            else -> {
                throw UnexpectedResponseType("Expected [BULK, SIMPLE_STRING, NULL] but got $code", input.tryInferCause(code))
            }
        }
    }
}
