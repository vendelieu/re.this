package eu.vendeli.rethis.codecs.server

import eu.vendeli.rethis.shared.decoders.aggregate.MapRTypeDecoder
import eu.vendeli.rethis.shared.types.*
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object AclGetUserCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("*3\r\n$3\r\nACL\r\n$7\r\nGETUSER\r\n")
    }

    public suspend fun encode(charset: Charset, username: String): CommandRequest {
        val buffer = Buffer()
        COMMAND_HEADER.copyTo(buffer)
        buffer.writeStringArg(username, charset, )

        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(charset: Charset, username: String): CommandRequest = encode(charset, username = username)

    public suspend fun decode(input: Buffer, charset: Charset): Map<String, RType>? {
        val code = input.parseCode(RespCode.ARRAY)
        return when(code) {
            RespCode.ARRAY -> {
                MapRTypeDecoder.decode(input, charset, code)
            }
            RespCode.MAP -> {
                MapRTypeDecoder.decode(input, charset, code)
            }
            RespCode.NULL -> {
                null
            }
            else -> {
                throw UnexpectedResponseType("Expected [ARRAY, MAP, NULL] but got $code", input.tryInferCause(code))
            }
        }
    }
}
