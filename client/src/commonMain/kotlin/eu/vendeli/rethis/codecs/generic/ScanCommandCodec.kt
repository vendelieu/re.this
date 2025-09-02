package eu.vendeli.rethis.codecs.generic

import eu.vendeli.rethis.shared.decoders.common.StringScanDecoder
import eu.vendeli.rethis.shared.request.generic.ScanOption
import eu.vendeli.rethis.shared.response.common.ScanResult
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
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
        var size = 1
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
