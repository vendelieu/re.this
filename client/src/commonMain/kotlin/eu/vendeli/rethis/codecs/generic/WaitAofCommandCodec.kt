package eu.vendeli.rethis.codecs.generic

import eu.vendeli.rethis.api.spec.common.decoders.generic.WaitAofDecoder
import eu.vendeli.rethis.api.spec.common.response.WaitAofResult
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import eu.vendeli.rethis.utils.writeLongArg
import eu.vendeli.rethis.utils.writeStringArg
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlin.Long
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object WaitAofCommandCodec {
    private const val BLOCKING_STATUS: Boolean = true

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("*4\r\n$7\r\nWAITAOF\r\n")
    }

    public suspend fun encode(
        charset: Charset,
        numlocal: Long,
        numreplicas: Long,
        timeout: Long,
    ): CommandRequest {
        val buffer = Buffer()
        COMMAND_HEADER.copyTo(buffer)
        buffer.writeLongArg(numlocal, charset, )
        buffer.writeLongArg(numreplicas, charset, )
        buffer.writeLongArg(timeout, charset, )

        return CommandRequest(buffer, RedisOperation.WRITE, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(
        charset: Charset,
        numlocal: Long,
        numreplicas: Long,
        timeout: Long,
    ): CommandRequest = encode(charset, numlocal = numlocal, numreplicas = numreplicas, timeout = timeout)

    public suspend fun decode(input: Buffer, charset: Charset): WaitAofResult = WaitAofDecoder.decode(input, charset)
}
