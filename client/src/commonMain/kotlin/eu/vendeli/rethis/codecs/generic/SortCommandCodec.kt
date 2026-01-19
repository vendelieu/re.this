package eu.vendeli.rethis.codecs.generic

import eu.vendeli.rethis.shared.decoders.aggregate.ArrayStringDecoder
import eu.vendeli.rethis.shared.request.generic.SortOption
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

public object SortCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$4\r\nSORT\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        vararg option: SortOption,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(key, charset)
        option.forEach { it0 ->
            when (it0) {
                is SortOption.ALPHA -> {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }

                is SortOption.By -> {
                    size += 1
                    buffer.writeStringArg("BY", charset)
                    size += 1
                    buffer.writeStringArg(it0.byPattern, charset)
                }

                is SortOption.Get -> {
                    size += 1
                    buffer.writeStringArg("GET", charset)
                    it0.getPattern.forEach { it1 ->
                        size += 1
                        buffer.writeStringArg(it1, charset)
                    }
                }

                is SortOption.Limit -> {
                    size += 1
                    buffer.writeStringArg("LIMIT", charset)
                    size += 1
                    buffer.writeLongArg(it0.offset, charset)
                    size += 1
                    buffer.writeLongArg(it0.count, charset)
                }

                is SortOption.Order -> {
                    when (it0) {
                        is SortOption.ASC -> {
                            size += 1
                            buffer.writeStringArg(it0.toString(), charset)
                        }

                        is SortOption.DESC -> {
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
        vararg option: SortOption,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key.toByteArray(charset)))
        option.forEach { it0 ->
            when (it0) {
                is SortOption.By -> {
                    slot = validateSlot(slot, CRC16.lookup(it0.byPattern.toByteArray(charset)))
                }

                is SortOption.Get -> {
                    it0.getPattern.forEach { it1 ->
                        slot = validateSlot(slot, CRC16.lookup(it1.toByteArray(charset)))
                    }
                }

                else -> {}
            }
        }
        if (slot == null) throw KeyAbsentException("Expected key is not provided")
        val request = encode(charset, key = key, option = option)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): List<String> {
        val code = input.parseCode(RespCode.ARRAY)
        return when (code) {
            RespCode.ARRAY -> {
                ArrayStringDecoder.decode(input, charset, code)
            }

            else -> {
                throw UnexpectedResponseType("Expected [ARRAY] but got $code", input.tryInferCause(code))
            }
        }
    }
}
