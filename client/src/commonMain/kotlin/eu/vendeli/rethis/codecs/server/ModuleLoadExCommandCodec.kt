package eu.vendeli.rethis.codecs.server

import eu.vendeli.rethis.api.spec.common.decoders.general.SimpleStringDecoder
import eu.vendeli.rethis.api.spec.common.request.server.ModuleOption
import eu.vendeli.rethis.api.spec.common.request.server.ModuleOption.Arguments
import eu.vendeli.rethis.api.spec.common.request.server.ModuleOption.Configs
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.String
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object ModuleLoadExCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$6\r\nMODULE\r\n$6\r\nLOADEX\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        path: String,
        vararg options: ModuleOption,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 0
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(path, charset, )
        options.forEach { it0 ->
            when (it0) {
                is ModuleOption.Arguments ->  {
                    size += 1
                    buffer.writeStringArg("ARGS", charset)
                    it0.args.forEach { it1 ->
                        size += 1
                        buffer.writeStringArg(it1, charset, )
                    }
                }
                is ModuleOption.Configs ->  {
                    size += 1
                    buffer.writeStringArg("CONFIG", charset)
                    size += 1
                    buffer.writeStringArg(it0.name, charset, )
                    size += 1
                    buffer.writeStringArg(it0.value, charset, )
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
        path: String,
        vararg options: ModuleOption,
    ): CommandRequest = encode(charset, path = path, options = options)

    public suspend fun decode(input: Buffer, charset: Charset): Boolean {
        val code = RespCode.fromCode(input.readByte())
        return when(code) {
            RespCode.SIMPLE_STRING -> {
                SimpleStringDecoder.decode(input, charset) == "OK"
            }
            else -> {
                throw UnexpectedResponseType("Expected [SIMPLE_STRING] but got $code", input.tryInferCause(code))
            }
        }
    }
}
