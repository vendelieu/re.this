package eu.vendeli.rethis.codecs.server

import eu.vendeli.rethis.api.spec.common.decoders.general.SimpleStringDecoder
import eu.vendeli.rethis.api.spec.common.request.server.ReplicaOfArgs
import eu.vendeli.rethis.api.spec.common.request.server.ReplicaOfArgs.HostPort
import eu.vendeli.rethis.api.spec.common.request.server.ReplicaOfArgs.NoOne
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object ReplicaOfCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("*2\r\n$9\r\nREPLICAOF\r\n")
    }

    public suspend fun encode(charset: Charset, args: ReplicaOfArgs): CommandRequest {
        val buffer = Buffer()
        COMMAND_HEADER.copyTo(buffer)
        when (args) {
            is ReplicaOfArgs.HostPort ->  {
                buffer.writeStringArg(args.host, charset, )
                buffer.writeLongArg(args.port, charset, )
            }
            is ReplicaOfArgs.NoOne ->  {
                buffer.writeStringArg("NO", charset)
                buffer.writeStringArg("ONE", charset)
            }
        }

        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(charset: Charset, args: ReplicaOfArgs): CommandRequest = encode(charset, args = args)

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
