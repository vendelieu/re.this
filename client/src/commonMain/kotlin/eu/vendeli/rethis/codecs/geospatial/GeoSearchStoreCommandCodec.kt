package eu.vendeli.rethis.codecs.geospatial

import eu.vendeli.rethis.shared.decoders.general.IntegerDecoder
import eu.vendeli.rethis.shared.request.geospatial.*
import eu.vendeli.rethis.shared.response.geospatial.GeoSort
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.UnexpectedResponseType
import eu.vendeli.rethis.shared.utils.CRC16
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.shared.utils.validateSlot
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeDoubleArg
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
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
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
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
        when (by) {
            is ByBox ->  {
                size += 1
                buffer.writeStringArg("BYBOX", charset)
                size += 1
                buffer.writeDoubleArg(by.width, charset, )
                size += 1
                buffer.writeDoubleArg(by.height, charset, )
                size += 1
                buffer.writeStringArg(by.unit.toString(), charset)
            }
            is ByRadius ->  {
                size += 1
                buffer.writeStringArg("BYRADIUS", charset)
                size += 1
                buffer.writeDoubleArg(by.radius, charset, )
                size += 1
                buffer.writeStringArg(by.unit.toString(), charset)
            }
        }
        order?.let { it0 ->
            size += 1
            buffer.writeStringArg(it0.toString(), charset)
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
        val code = input.parseCode(RespCode.INTEGER)
        return when(code) {
            RespCode.INTEGER -> {
                IntegerDecoder.decode(input, charset, code)
            }
            else -> {
                throw UnexpectedResponseType("Expected [INTEGER] but got $code", input.tryInferCause(code))
            }
        }
    }
}
