package eu.vendeli.rethis.codecs.generic

import eu.vendeli.rethis.api.spec.common.decoders.general.SimpleStringDecoder
import eu.vendeli.rethis.api.spec.common.request.generic.MigrateKey
import eu.vendeli.rethis.api.spec.common.request.generic.MigrateKey.Actual
import eu.vendeli.rethis.api.spec.common.request.generic.MigrateKey.Empty
import eu.vendeli.rethis.api.spec.common.request.generic.MigrateOption
import eu.vendeli.rethis.api.spec.common.request.generic.MigrateOption.Auth
import eu.vendeli.rethis.api.spec.common.request.generic.MigrateOption.Auth2
import eu.vendeli.rethis.api.spec.common.request.generic.MigrateOption.Authorization
import eu.vendeli.rethis.api.spec.common.request.generic.MigrateOption.COPY
import eu.vendeli.rethis.api.spec.common.request.generic.MigrateOption.Keys
import eu.vendeli.rethis.api.spec.common.request.generic.MigrateOption.REPLACE
import eu.vendeli.rethis.api.spec.common.request.generic.MigrateOption.Strategy
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.KeyAbsentException
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.TimeUnit
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.api.spec.common.utils.validateSlot
import eu.vendeli.rethis.utils.writeDurationArg
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlin.time.Duration
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object MigrateCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$7\r\nMIGRATE\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        host: String,
        port: Long,
        keySelector: MigrateKey,
        destinationDb: Long,
        timeout: Duration,
        vararg option: MigrateOption,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(host, charset, )
        size += 1
        buffer.writeLongArg(port, charset, )
        when (keySelector) {
            is MigrateKey.Actual ->  {
                size += 1
                buffer.writeStringArg(keySelector.key, charset, )
            }
            is MigrateKey.Empty ->  {
                size += 1
                buffer.writeStringArg("", charset)
            }
        }
        size += 1
        buffer.writeLongArg(destinationDb, charset, )
        size += 1
        buffer.writeDurationArg(timeout, charset, TimeUnit.MILLISECONDS)
        option.forEach { it0 ->
            when (it0) {
                is MigrateOption.Authorization ->  {
                    when (it0) {
                        is MigrateOption.Auth ->  {
                            size += 1
                            buffer.writeStringArg("AUTH", charset)
                            size += 1
                            buffer.writeStringArg(it0.auth, charset, )
                        }
                        is MigrateOption.Auth2 ->  {
                            size += 1
                            buffer.writeStringArg("AUTH2", charset)
                            size += 1
                            buffer.writeStringArg(it0.username, charset, )
                            size += 1
                            buffer.writeStringArg(it0.password, charset, )
                        }
                    }
                }
                is MigrateOption.Keys ->  {
                    size += 1
                    buffer.writeStringArg("KEYS", charset)
                    it0.keys.forEach { it1 ->
                        size += 1
                        buffer.writeStringArg(it1, charset, )
                    }
                }
                is MigrateOption.Strategy ->  {
                    when (it0) {
                        is MigrateOption.COPY ->  {
                            size += 1
                            buffer.writeStringArg(it0.toString(), charset)
                        }
                        is MigrateOption.REPLACE ->  {
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
        host: String,
        port: Long,
        keySelector: MigrateKey,
        destinationDb: Long,
        timeout: Duration,
        vararg option: MigrateOption,
    ): CommandRequest {
        var slot: Int? = null
        when (keySelector) {
            is MigrateKey.Actual ->  {
                slot = validateSlot(slot, CRC16.lookup(keySelector.key.toByteArray(charset)))
            }
            else -> {}
        }
        option.forEach { it0 ->
            when (it0) {
                is MigrateOption.Keys ->  {
                    it0.keys.forEach { it1 ->
                        slot = validateSlot(slot, CRC16.lookup(it1.toByteArray(charset)))
                    }
                }
                else -> {}
            }
        }
        if(slot == null) throw KeyAbsentException("Expected key is not provided")
        val request = encode(charset, host = host, port = port, keySelector = keySelector, destinationDb = destinationDb, timeout = timeout, option = option)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): String {
        val code = RespCode.fromCode(input.readByte())
        return when(code) {
            RespCode.SIMPLE_STRING -> {
                SimpleStringDecoder.decode(input, charset)
            }
            else -> {
                throw UnexpectedResponseType("Expected [SIMPLE_STRING] but got $code", input.tryInferCause(code))
            }
        }
    }
}
