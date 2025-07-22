package eu.vendeli.rethis.codecs.json

import eu.vendeli.rethis.api.spec.common.decoders.general.BulkStringDecoder
import eu.vendeli.rethis.api.spec.common.request.json.JsonGetOption
import eu.vendeli.rethis.api.spec.common.request.json.JsonGetOption.Indent
import eu.vendeli.rethis.api.spec.common.request.json.JsonGetOption.Newline
import eu.vendeli.rethis.api.spec.common.request.json.JsonGetOption.Paths
import eu.vendeli.rethis.api.spec.common.request.json.JsonGetOption.Space
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.String
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object JsonGetCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$8\r\nJSON.GET\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        key: String,
        vararg options: JsonGetOption,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(key, charset, )
        options.forEach { it0 ->
            when (it0) {
                is JsonGetOption.Indent ->  {
                    size += 1
                    buffer.writeStringArg("INDENT", charset)
                    size += 1
                    buffer.writeStringArg(it0.indent, charset, )
                }
                is JsonGetOption.Newline ->  {
                    size += 1
                    buffer.writeStringArg("NEWLINE", charset)
                    size += 1
                    buffer.writeStringArg(it0.newline, charset, )
                }
                is JsonGetOption.Paths ->  {
                    it0.path.forEach { it1 ->
                        size += 1
                        buffer.writeStringArg(it1, charset, )
                    }
                }
                is JsonGetOption.Space ->  {
                    size += 1
                    buffer.writeStringArg("SPACE", charset)
                    size += 1
                    buffer.writeStringArg(it0.space, charset, )
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
        vararg options: JsonGetOption,
    ): CommandRequest = encode(charset, key = key, options = options)

    public suspend fun decode(input: Buffer, charset: Charset): String? {
        val code = input.parseCode(RespCode.BULK)
        return when(code) {
            RespCode.BULK -> {
                BulkStringDecoder.decode(input, charset, code)
            }
            RespCode.NULL -> {
                null
            }
            else -> {
                throw UnexpectedResponseType("Expected [BULK, NULL] but got $code", input.tryInferCause(code))
            }
        }
    }
}
