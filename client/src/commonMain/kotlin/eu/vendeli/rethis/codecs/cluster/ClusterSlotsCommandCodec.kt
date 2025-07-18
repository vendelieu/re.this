package eu.vendeli.rethis.codecs.cluster

import eu.vendeli.rethis.api.spec.common.decoders.cluster.ClusterSlotsDecoder
import eu.vendeli.rethis.api.spec.common.response.cluster.Cluster
import eu.vendeli.rethis.api.spec.common.types.CommandRequest
import eu.vendeli.rethis.api.spec.common.types.RedisOperation
import eu.vendeli.rethis.api.spec.common.utils.CRC16
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.core.toByteArray
import kotlin.Boolean
import kotlinx.io.Buffer
import kotlinx.io.writeString

public object ClusterSlotsCommandCodec {
    private const val BLOCKING_STATUS: Boolean = false

    private val COMMAND_HEADER: Buffer = Buffer().apply {
        writeString("*2\r\n$7\r\nCLUSTER\r\n$5\r\nSLOTS\r\n")
    }

    public suspend fun encode(charset: Charset): CommandRequest {
        val buffer = Buffer()
        COMMAND_HEADER.copyTo(buffer)

        return CommandRequest(buffer, RedisOperation.READ, BLOCKING_STATUS)
    }

    public suspend inline fun encodeWithSlot(charset: Charset): CommandRequest = encode(charset, )

    public suspend fun decode(input: Buffer, charset: Charset): Cluster = ClusterSlotsDecoder.decode(input, charset)
}
