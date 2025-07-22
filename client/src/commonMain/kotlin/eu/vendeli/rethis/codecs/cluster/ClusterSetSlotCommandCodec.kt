package eu.vendeli.rethis.codecs.cluster

import eu.vendeli.rethis.api.spec.common.decoders.general.SimpleStringDecoder
import eu.vendeli.rethis.api.spec.common.request.cluster.ClusterSetSlotOption
import eu.vendeli.rethis.api.spec.common.request.cluster.ClusterSetSlotOption.Importing
import eu.vendeli.rethis.api.spec.common.request.cluster.ClusterSetSlotOption.Migrating
import eu.vendeli.rethis.api.spec.common.request.cluster.ClusterSetSlotOption.Node
import eu.vendeli.rethis.api.spec.common.request.cluster.ClusterSetSlotOption.STABLE
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.UnexpectedResponseType
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import eu.vendeli.rethis.utils.parseCode
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.Long
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object ClusterSetSlotCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("*4\r\n$7\r\nCLUSTER\r\n$7\r\nSETSLOT\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        slot: Long,
        subcommand: ClusterSetSlotOption,
    ): CommandRequest {
        val buffer = Buffer()
        COMMAND_HEADER.copyTo(buffer)
        buffer.writeLongArg(slot, charset, )
        when (subcommand) {
            is ClusterSetSlotOption.Importing ->  {
                buffer.writeStringArg("IMPORTING", charset)
                buffer.writeStringArg(subcommand.nodeId, charset, )
            }
            is ClusterSetSlotOption.Migrating ->  {
                buffer.writeStringArg("MIGRATING", charset)
                buffer.writeStringArg(subcommand.nodeId, charset, )
            }
            is ClusterSetSlotOption.Node ->  {
                buffer.writeStringArg("NODE", charset)
                buffer.writeStringArg(subcommand.nodeId, charset, )
            }
            is ClusterSetSlotOption.STABLE ->  {
                buffer.writeStringArg(subcommand.toString(), charset)
            }
        }

        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        slot: Long,
        subcommand: ClusterSetSlotOption,
    ): CommandRequest = encode(charset, slot = slot, subcommand = subcommand)

    public suspend fun decode(input: Buffer, charset: Charset): Boolean {
        val code = input.parseCode(RespCode.SIMPLE_STRING)
        return when(code) {
            RespCode.SIMPLE_STRING -> {
                SimpleStringDecoder.decode(input, charset, code) == "OK"
            }
            else -> {
                throw UnexpectedResponseType("Expected [SIMPLE_STRING] but got $code", input.tryInferCause(code))
            }
        }
    }
}
