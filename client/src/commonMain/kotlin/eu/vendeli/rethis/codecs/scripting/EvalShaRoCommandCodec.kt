package eu.vendeli.rethis.codecs.scripting

import eu.vendeli.rethis.api.spec.common.decoders.general.RTypeDecoder
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.KeyAbsentException
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.api.spec.common.utils.validateSlot
import eu.vendeli.rethis.utils.writeIntArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object EvalShaRoCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("\r\n$10\r\nEVALSHA_RO\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        sha1: String,
        vararg key: String,
        arg: List<String>,
    ): CommandRequest {
        var buffer = Buffer()
        var size = 1
        COMMAND_HEADER.copyTo(buffer)
        size += 1
        buffer.writeStringArg(sha1, charset, )
        size += 1
        buffer.writeIntArg(key.size, charset)
        key.forEach { it0 ->
            size += 1
            buffer.writeStringArg(it0, charset, )
        }
        arg.forEach { it1 ->
            size += 1
            buffer.writeStringArg(it1, charset, )
        }

        buffer = Buffer().apply {
            writeString("*$size")
            transferFrom(buffer)
        }
        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        sha1: String,
        vararg key: String,
        arg: List<String>,
    ): CommandRequest {
        var slot: Int? = null
        key.forEach { it0 ->
            slot = validateSlot(slot, CRC16.lookup(it0.toByteArray(charset)))
        }
        if (slot == null) throw KeyAbsentException("Expected key is not provided")
        val request = encode(charset, sha1 = sha1, key = key, arg = arg)
        return request.withSlot(slot % 16384)
    }

    public suspend fun decode(input: Buffer, charset: Charset): RType = RTypeDecoder.decode(input, charset)
}
