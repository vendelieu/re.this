package eu.vendeli.rethis.types.common

import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.io.Buffer
import kotlinx.io.InternalIoApi

data class RConnection(
    val socket: Socket,
    val input: ByteReadChannel,
    val output: ByteWriteChannel,
) {
    @OptIn(InternalAPI::class, InternalIoApi::class)
    suspend fun doRequest(payload: Buffer): Buffer {
        output.runCatching {
            writeBuffer.transferFrom(payload)
            flushIfNeeded()
        }.onFailure {
            cleanup()
            throw it
        }
        input.awaitContent() // todo look for cases where this is not needed (subscribe for example)
        return Buffer().apply { transferFrom(input.readBuffer) }
    }

    @OptIn(InternalAPI::class, InternalIoApi::class)
    suspend fun doBatchRequest(payload: List<Buffer>): Buffer {
        output.runCatching {
            payload.forEach { writeBuffer.transferFrom(it) }
            flushIfNeeded()
        }.onFailure {
            cleanup()
            throw it
        }
        input.awaitContent()
        return Buffer().apply { transferFrom(input.readBuffer) }
    }

    @OptIn(InternalAPI::class, InternalIoApi::class)
    private inline fun cleanup() {
        if (input.availableForRead > 0) input.readBuffer.buffer.clear()
        if (output.availableForWrite > 0) output.writeBuffer.buffer.clear()
    }
}

internal fun Socket.rConnection(): RConnection = RConnection(this, openReadChannel(), openWriteChannel())
