package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.annotations.ReThisInternal
import eu.vendeli.rethis.shared.utils.readCompleteResponseInto
import eu.vendeli.rethis.utils.COMMON_LOGGER
import io.ktor.network.sockets.*
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import kotlinx.io.Buffer
import kotlinx.io.InternalIoApi
import kotlinx.io.readString

data class RConnection(
    val socket: Socket,
    val input: ByteReadChannel,
    val output: ByteWriteChannel,
) {
    @ReThisInternal
    @OptIn(InternalAPI::class, InternalIoApi::class)
    suspend fun doRequest(payload: Buffer): Buffer {
        COMMON_LOGGER.trace { "Request:\n${payload.copy().readString()}" }

        output.writeBuffer.transferFrom(payload)
        output.flush()

        val response = Buffer()
        input.readCompleteResponseInto(response)

        COMMON_LOGGER.trace { "Response:\n${response.copy().readString()}" }
        return response
    }

    @ReThisInternal
    @OptIn(InternalAPI::class, InternalIoApi::class)
    suspend fun doBatchRequest(payload: List<Buffer>): Buffer {
        COMMON_LOGGER.trace {
            "Request:\n${payload.joinToString("\n") { it.copy().readString() }}"
        }
        for (request in payload) {
            output.writeBuffer.transferFrom(request)
        }
        output.flush()

        val response = Buffer()
        repeat(payload.size) {
            input.readCompleteResponseInto(response)
        }

        COMMON_LOGGER.trace { "Response:\n${response.copy().readString()}" }
        return response
    }
}

internal fun Socket.rConnection(): RConnection =
    RConnection(
        this,
        openReadChannel(),
        openWriteChannel(),
    )
