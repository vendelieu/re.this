package eu.vendeli.rethis.codecs.geospatial

import eu.vendeli.rethis.api.spec.common.decoders.general.IntegerDecoder
import eu.vendeli.rethis.api.spec.common.request.geospatial.ByBox
import eu.vendeli.rethis.api.spec.common.request.geospatial.ByRadius
import eu.vendeli.rethis.api.spec.common.request.geospatial.CenterPoint
import eu.vendeli.rethis.api.spec.common.request.geospatial.FromLongitudeLatitude
import eu.vendeli.rethis.api.spec.common.request.geospatial.FromMember
import eu.vendeli.rethis.api.spec.common.request.geospatial.Shape
import eu.vendeli.rethis.api.spec.common.response.geospatial.GeoSort
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.api.spec.common.utils.validateSlot
import eu.vendeli.rethis.utils.writeDoubleArg
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object GeoSearchStoreCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$14\r\nGEOSEARCHSTORE\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        destination: String,
        source: String,
        from: CenterPoint,
        `by`: Shape,
        order: GeoSort?,
        count: Long?,
        any: Boolean?,
        storedist: Boolean?,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 0
        COMMAND_HEADER.copyTo(buffer)
        when (by) {
            is ByBox ->  {
                size += 1
                buffer.writeStringArg("BYBOX", charset)
                size += 1
                buffer.writeStringArg(by.toString(), charset)
                size += 1
                buffer.writeDoubleArg(by.width, charset, )
                size += 1
                buffer.writeDoubleArg(by.height, charset, )
            }
            is ByRadius ->  {
                size += 1
                buffer.writeStringArg("BYRADIUS", charset)
                size += 1
                buffer.writeStringArg(by.toString(), charset)
                size += 1
                buffer.writeDoubleArg(by.radius, charset, )
            }
        }
        order?.let { it0 ->
            size += 1
            buffer.writeStringArg(it0.toString(), charset)
        }
        size += 1
        buffer.writeStringArg(destination, charset, )
        size += 1
        buffer.writeStringArg(source, charset, )
        when (from) {
            is FromLongitudeLatitude ->  {
                size += 1
                buffer.writeStringArg("FROMLONLAT", charset)
                size += 1
                buffer.writeDoubleArg(from.longitude, charset, )
                size += 1
                buffer.writeDoubleArg(from.latitude, charset, )
            }
            is FromMember ->  {
                size += 1
                buffer.writeStringArg("FROMMEMBER", charset)
                size += 1
                buffer.writeStringArg(from.member, charset, )
            }
        }
        count?.let { it1 ->
            size += 1
            buffer.writeStringArg("COUNT", charset)
            size += 1
            buffer.writeLongArg(it1, charset, )
        }
        any?.let { it2 ->
            if(it2) {
                size += 1
                buffer.writeStringArg("ANY", charset)
            }
        }
        storedist?.let { it3 ->
            if(it3) {
                size += 1
                buffer.writeStringArg("STOREDIST", charset)
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
        destination: String,
        source: String,
        from: CenterPoint,
        `by`: Shape,
        order: GeoSort?,
        count: Long?,
        any: Boolean?,
        storedist: Boolean?,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(destination.toByteArray(charset)))
        slot = validateSlot(slot, CRC16.lookup(source.toByteArray(charset)))
        val request = encode(charset, destination = destination, source = source, from = from, by = by, order = order, count = count, any = any, storedist = storedist)
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
