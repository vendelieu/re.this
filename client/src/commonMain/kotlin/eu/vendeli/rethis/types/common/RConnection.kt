package eu.vendeli.rethis.types.common

import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.Buffer
import kotlinx.io.InternalIoApi

internal data class RConnection(
    val socket: Socket,
    val input: ByteReadChannel,
    val output: ByteWriteChannel,
) {
    private val state = Mutex()

    @OptIn(InternalAPI::class, InternalIoApi::class)
    suspend fun doRequest(payload: Buffer): Buffer = state.withLock {
        output.runCatching {
            writeBuffer.transferFrom(payload)
            flush()
        }.onFailure {
            if (input.availableForRead > 0) input.readBuffer.buffer.clear()
            if (output.availableForWrite > 0) output.writeBuffer.buffer.clear()
            throw it
        }
        input.awaitContent()
        Buffer().apply { transferFrom(input.readBuffer) }
    }

    @OptIn(InternalAPI::class, InternalIoApi::class)
    suspend fun doRequest(payload: List<Buffer>): Buffer = state.withLock {
        output.runCatching {
            payload.forEach { writeBuffer.transferFrom(it) }
            flush()
        }.onFailure {
            if (input.availableForRead > 0) input.readBuffer.buffer.clear()
            if (output.availableForWrite > 0) output.writeBuffer.buffer.clear()
            throw it
        }
        input.awaitContent()
        Buffer().apply { transferFrom(input.readBuffer) }
    }
}

internal fun Socket.rConnection(): RConnection = RConnection(this, openReadChannel(), openWriteChannel())
