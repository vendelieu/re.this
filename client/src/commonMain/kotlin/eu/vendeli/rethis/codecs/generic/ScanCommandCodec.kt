package eu.vendeli.rethis.codecs.generic

import eu.vendeli.rethis.api.spec.common.decoders.common.StringScanDecoder
import eu.vendeli.rethis.api.spec.common.request.generic.ScanOption
import eu.vendeli.rethis.api.spec.common.request.generic.ScanOption.Count
import eu.vendeli.rethis.api.spec.common.request.generic.ScanOption.Match
import eu.vendeli.rethis.api.spec.common.request.generic.ScanOption.Type
import eu.vendeli.rethis.api.spec.common.response.ScanResult
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.Long
import kotlin.String
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object ScanCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$4\r\nSCAN\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        cursor: Long,
        vararg option: ScanOption,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 0
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeLongArg(cursor, charset, )
        option.forEach { it0 ->
            when (it0) {
                is ScanOption.Count ->  {
                    size += 1
                    buffer.writeStringArg("COUNT", charset)
                    size += 1
                    buffer.writeLongArg(it0.count, charset, )
                }
                is ScanOption.Match ->  {
                    size += 1
                    buffer.writeStringArg("MATCH", charset)
                    size += 1
                    buffer.writeStringArg(it0.pattern, charset, )
                }
                is ScanOption.Type ->  {
                    size += 1
                    buffer.writeStringArg("TYPE", charset)
                    size += 1
                    buffer.writeStringArg(it0.type, charset, )
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
        cursor: Long,
        vararg option: ScanOption,
    ): CommandRequest = encode(charset, cursor = cursor, option = option)

    public suspend fun decode(input: Buffer, charset: Charset): ScanResult<String> = StringScanDecoder.decode(input, charset)
}
