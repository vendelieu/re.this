package eu.vendeli.rethis.codecs.sentinel

import eu.vendeli.rethis.api.spec.common.decoders.aggregate.ArrayRTypeDecoder
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RType
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
import kotlin.collections.List
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object SentinelReplicasCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("*3\r\n$8\r\nSENTINEL\r\n$8\r\nREPLICAS\r\n")
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
