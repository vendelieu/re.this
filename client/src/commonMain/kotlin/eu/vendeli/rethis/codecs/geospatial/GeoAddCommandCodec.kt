package eu.vendeli.rethis.codecs.geospatial

import eu.vendeli.rethis.api.spec.common.decoders.general.IntegerDecoder
import eu.vendeli.rethis.api.spec.common.request.geospatial.GeoAddOption
import eu.vendeli.rethis.api.spec.common.request.geospatial.GeoAddOption.NX
import eu.vendeli.rethis.api.spec.common.request.geospatial.GeoAddOption.XX
import eu.vendeli.rethis.api.spec.common.response.geospatial.GeoMember
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.api.spec.common.utils.validateSlot
import eu.vendeli.rethis.utils.writeDoubleArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object GeoAddCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$6\r\nGEOADD\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        vararg `data`: GeoMember,
        condition: GeoAddOption.UpsertMode?,
        change: Boolean?,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(key, charset, )
        condition?.let { it0 ->
            when (it0) {
                is GeoAddOption.NX ->  {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }
                is GeoAddOption.XX ->  {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }
            }
        }
        change?.let { it1 ->
            if(it1) {
                size += 1
                buffer.writeStringArg("CH", charset)
            }
        }
        data.forEach { it2 ->
            size += 1
            buffer.writeDoubleArg(it2.longitude, charset, )
            size += 1
            buffer.writeDoubleArg(it2.latitude, charset, )
            size += 1
            buffer.writeStringArg(it2.member, charset, )
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
        vararg `data`: GeoMember,
        condition: GeoAddOption.UpsertMode?,
        change: Boolean?,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key.toByteArray(charset)))
        val request = encode(charset, key = key, data = data, condition = condition, change = change)
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
