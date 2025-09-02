package eu.vendeli.rethis.codecs.sentinel

import eu.vendeli.rethis.shared.decoders.aggregate.ArrayRTypeDecoder
import eu.vendeli.rethis.shared.types.*
import eu.vendeli.rethis.shared.utils.tryInferCause
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object SentinelSentinelsCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("*3\r\n$8\r\nSENTINEL\r\n$9\r\nSENTINELS\r\n")
    }

    public suspend fun encode(charset: Charset, masterName: String): CommandRequest {
        val buffer = Buffer()
        COMMAND_HEADER.copyTo(buffer)
        buffer.writeStringArg(masterName, charset, )

        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(charset: Charset, masterName: String): CommandRequest = encode(charset, masterName = masterName)

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
