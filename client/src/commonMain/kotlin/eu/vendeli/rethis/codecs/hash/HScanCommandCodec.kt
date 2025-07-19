package eu.vendeli.rethis.codecs.hash

import eu.vendeli.rethis.api.spec.common.decoders.common.PairScanDecoder
import eu.vendeli.rethis.api.spec.common.request.hash.HScanOption
import eu.vendeli.rethis.api.spec.common.request.hash.HScanOption.Count
import eu.vendeli.rethis.api.spec.common.request.hash.HScanOption.Match
import eu.vendeli.rethis.api.spec.common.request.hash.HScanOption.NOVALUES
import eu.vendeli.rethis.api.spec.common.response.common.ScanResult
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.validateSlot
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.Long
import kotlin.Pair
import kotlin.String
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object HScanCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$5\r\nHSCAN\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        cursor: Long,
        vararg option: HScanOption,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 0
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(key, charset, )
        size += 1
        buffer.writeLongArg(cursor, charset, )
        option.forEach { it0 ->
            when (it0) {
                is HScanOption.Count ->  {
                    size += 1
                    buffer.writeStringArg("COUNT", charset)
                    size += 1
                    buffer.writeLongArg(it0.count, charset, )
                }
                is HScanOption.Match ->  {
                    size += 1
                    buffer.writeStringArg("MATCH", charset)
                    size += 1
                    buffer.writeStringArg(it0.pattern, charset, )
                }
                is HScanOption.NOVALUES ->  {
                    size += 1
                    buffer.writeStringArg(it0.toString(), charset)
                }
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
        cursor: Long,
        vararg option: HScanOption,
    ): CommandRequest {
        var slot: Int? = null
        slot = validateSlot(slot, CRC16.lookup(key.toByteArray(charset)))
        val request = encode(charset, key = key, cursor = cursor, option = option)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): ScanResult<Pair<String, String>> = PairScanDecoder.decode(input, charset)
}
