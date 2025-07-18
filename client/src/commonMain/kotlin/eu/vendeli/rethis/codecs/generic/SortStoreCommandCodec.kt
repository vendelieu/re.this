package eu.vendeli.rethis.codecs.generic

import eu.vendeli.rethis.api.spec.common.decoders.general.IntegerDecoder
import eu.vendeli.rethis.api.spec.common.request.generic.SortOption
import eu.vendeli.rethis.api.spec.common.request.generic.SortOption.ALPHA
import eu.vendeli.rethis.api.spec.common.request.generic.SortOption.ASC
import eu.vendeli.rethis.api.spec.common.request.generic.SortOption.By
import eu.vendeli.rethis.api.spec.common.request.generic.SortOption.DESC
import eu.vendeli.rethis.api.spec.common.request.generic.SortOption.Get
import eu.vendeli.rethis.api.spec.common.request.generic.SortOption.Limit
import eu.vendeli.rethis.api.spec.common.request.generic.SortOption.Order
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.KeyAbsentException
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.api.spec.common.utils.validateSlot
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object SortStoreCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$4\r\nSORT\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        storeDestination: String,
        vararg option: SortOption,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 0
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(key, charset, )
        option.forEach { it0 ->
            when (it0) {
                is SortOption.ALPHA ->  {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }
                is SortOption.By ->  {
                    size += 1
                    buffer.writeStringArg("BY", charset)
                    size += 1
                    buffer.writeStringArg(it0.byPattern, charset, )
                }
                is SortOption.Get ->  {
                    size += 1
                    buffer.writeStringArg("GET", charset)
                    it0.getPattern.forEach { it1 ->
                        size += 1
                        buffer.writeStringArg(it1, charset, )
                    }
                }
                is SortOption.Limit ->  {
                    size += 1
                    buffer.writeStringArg("LIMIT", charset)
                    size += 1
                    buffer.writeLongArg(it0.offset, charset, )
                    size += 1
                    buffer.writeLongArg(it0.count, charset, )
                }
                is SortOption.Order ->  {
                    when (it0) {
                        is SortOption.ASC ->  {
                            size += 1
                            buffer.writeStringArg(it0.toString(), charset)
                        }
                        is SortOption.DESC ->  {
                            size += 1
                            buffer.writeStringArg(it0.toString(), charset)
                        }
                    }
                }
            }
        }
        size += 1
        buffer.writeStringArg("STORE", charset)
        size += 1
        buffer.writeStringArg(storeDestination, charset, )

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        key: String,
        storeDestination: String,
        vararg option: SortOption,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key.toByteArray(charset)))
        option.forEach { it0 ->
            when (it0) {
                is SortOption.By ->  {
                    slot = validateSlot(slot, CRC16.lookup(it0.byPattern.toByteArray(charset)))
                }
                is SortOption.Get ->  {
                    it0.getPattern.forEach { it1 ->
                        slot = validateSlot(slot, CRC16.lookup(it1.toByteArray(charset)))
                    }
                }
                else -> {}
            }
        }
        slot = validateSlot(slot, CRC16.lookup(storeDestination.toByteArray(charset)))
        if(slot == null) throw KeyAbsentException("Expected key is not provided")
        val request = encode(charset, key = key, storeDestination = storeDestination, option = option)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): Long {
        val code = RespCode.fromCode(input.readByte())
        return when(code) {
            RespCode.INTEGER -> {
                IntegerDecoder.decode(input, charset)
            }
            else -> {
                throw UnexpectedResponseType("Expected [INTEGER] but got $code", input.tryInferCause(code))
            }
        }
    }
}
