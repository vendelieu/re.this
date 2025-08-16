package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.annotations.ReThisInternal
import eu.vendeli.rethis.utils.COMMON_LOGGER
import io.ktor.network.sockets.*
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import kotlinx.io.Buffer
import kotlinx.io.InternalIoApi
import kotlinx.io.bytestring.decodeToString
import kotlinx.io.readByteString

data class RConnection(
    val socket: Socket,
    val input: ByteReadChannel,
    val output: ByteWriteChannel,
) {
    @ReThisInternal
    @OptIn(InternalAPI::class, InternalIoApi::class)
    suspend fun doRequest(payload: Buffer): Buffer {
        COMMON_LOGGER.trace { "Request:\n${payload.copy().readByteString().decodeToString()}" }
        val response = request {
            writeBuffer.transferFrom(payload)
        }
        COMMON_LOGGER.trace { "Response:\n${response.copy().readByteString().decodeToString()}" }
        return response
    }

    @ReThisInternal
    @OptIn(InternalAPI::class, InternalIoApi::class)
    suspend fun doBatchRequest(payload: List<Buffer>): Buffer {
        COMMON_LOGGER.trace {
            "Request:\n${
                payload.joinToString("\n") { it.copy().readByteString().decodeToString() }
            }"
        }
        val response = request {
            payload.forEach { writeBuffer.transferFrom(it) }
        }
        COMMON_LOGGER.trace { "Response:\n${response.copy().readByteString().decodeToString()}" }
        return response
    }

    @OptIn(InternalAPI::class, InternalIoApi::class)
    private suspend inline fun request(payloadBlock: ByteWriteChannel.() -> Unit): Buffer {
        output.runCatching {
            payloadBlock()
            flush()
        }.onFailure {
            if (input.availableForRead > 0) input.readBuffer.buffer.clear()
            if (output.availableForWrite > 0) output.writeBuffer.buffer.clear()
            throw it
        }
        val response = Buffer()
        input.readBuffer.transferTo(response)

        if (response.exhausted()) {
            input.awaitContent()
            input.readBuffer.transferTo(response)
        }

        return response
    }
}

internal fun Socket.rConnection(): RConnection = RConnection(this, openReadChannel(), openWriteChannel())
