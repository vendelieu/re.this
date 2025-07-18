package eu.vendeli.rethis.codecs.server

import eu.vendeli.rethis.api.spec.common.decoders.general.BulkStringDecoder
import eu.vendeli.rethis.api.spec.common.decoders.general.VerbatimStringDecoder
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.String
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object MemoryDoctorCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("*2\r\n$6\r\nMEMORY\r\n$6\r\nDOCTOR\r\n")
    }

    public suspend fun encode(charset: Charset): CommandRequest {
        val buffer = Buffer()
        COMMAND_HEADER.copyTo(buffer)

        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(charset: Charset): CommandRequest = encode(charset, )

    public suspend fun decode(input: Buffer, charset: Charset): String {
        val code = RespCode.fromCode(input.readByte())
        return when(code) {
            RespCode.BULK -> {
                BulkStringDecoder.decode(input, charset)
            }
            RespCode.VERBATIM_STRING -> {
                VerbatimStringDecoder.decode(input, charset)
            }
            else -> {
                throw UnexpectedResponseType("Expected [BULK, VERBATIM_STRING] but got $code", input.tryInferCause(code))
            }
        }
    }
}
