package eu.vendeli.rethis.types.core

import eu.vendeli.rethis.utils.bufferValues
import eu.vendeli.rethis.utils.parseResponse
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.io.Buffer
import kotlinx.io.InternalIoApi

internal data class RConnection(
    val socket: Socket,
    val input: ByteReadChannel,
    val output: ByteWriteChannel,
) {
    private val state = Mutex()

    @OptIn(InternalAPI::class, InternalIoApi::class)
    suspend fun writeRequest(payload: Buffer): RConnection {
        state.lock()
        try {
            output.writeBuffer.transferFrom(payload)
            output.flush()
        } catch (e: Exception) {
            if (input.availableForRead > 0) input.readBuffer.buffer.clear()
            if (output.availableForWrite > 0) output.writeBuffer.buffer.clear()
            state.unlock()
            throw e
        }

        return this
    }

    suspend inline fun writeRequest(payload: List<Argument>, charset: Charset): RConnection =
        writeRequest(bufferValues(payload, charset))

    suspend fun readResponse(): ArrayDeque<ResponseToken> = try {
        input.parseResponse()
    } finally {
        state.unlock()
    }

    suspend fun readBatchResponse(count: Int): List<ArrayDeque<ResponseToken>> = try {
        (1..count).map { input.parseResponse() }
    } finally {
        state.unlock()
    }
}

internal fun Socket.rConnection(): RConnection = RConnection(this, openReadChannel(), openWriteChannel())
