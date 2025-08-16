package eu.vendeli.rethis.codecs.geospatial

import eu.vendeli.rethis.api.spec.common.decoders.aggregate.ArrayRTypeDecoder
import eu.vendeli.rethis.api.spec.common.request.geospatial.*
import eu.vendeli.rethis.api.spec.common.response.geospatial.GeoSort
import eu.vendeli.rethis.api.spec.common.types.*
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.api.spec.common.utils.validateSlot
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeDoubleArg
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object GeoSearchCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$9\r\nGEOSEARCH\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        from: CenterPoint,
        `by`: Shape,
        withCoord: Boolean?,
        withDist: Boolean?,
        withHash: Boolean?,
        count: Long?,
        any: Boolean?,
        order: GeoSort?,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(key, charset, )
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
        withCoord?.let { it3 ->
            if(it3) {
                size += 1
                buffer.writeStringArg("WITHCOORD", charset)
            }
        }
        withDist?.let { it4 ->
            if(it4) {
                size += 1
                buffer.writeStringArg("WITHDIST", charset)
            }
        }
        withHash?.let { it5 ->
            if(it5) {
                size += 1
                buffer.writeStringArg("WITHHASH", charset)
            }
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
        from: CenterPoint,
        `by`: Shape,
        withCoord: Boolean?,
        withDist: Boolean?,
        withHash: Boolean?,
        count: Long?,
        any: Boolean?,
        order: GeoSort?,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key.toByteArray(charset)))
        val request = encode(charset, key = key, from = from, by = by, withCoord = withCoord, withDist = withDist, withHash = withHash, count = count, any = any, order = order)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): List<RType> {
        val code = input.parseCode(RespCode.ARRAY)
        return when(code) {
            RespCode.ARRAY -> {
                ArrayRTypeDecoder.decode(input, charset, code)
            }
            else -> {
                throw UnexpectedResponseType("Expected [ARRAY] but got $code", input.tryInferCause(code))
            }
        }
    }
}
