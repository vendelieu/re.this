package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.annotations.ReThisInternal
import eu.vendeli.rethis.shared.types.CommandTimeoutException
import eu.vendeli.rethis.shared.utils.readCompleteResponseInto
import eu.vendeli.rethis.utils.COMMON_LOGGER
import io.ktor.network.sockets.*
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.io.Buffer
import kotlinx.io.InternalIoApi
import kotlinx.io.readString
import kotlin.time.Duration
import kotlin.time.TimeSource

data class RConnection(
    val socket: Socket,
    val input: ByteReadChannel,
    val output: ByteWriteChannel,
) {
    /**
     * Monotonic timestamp of the last successful release back to the pool. Used by
     * the pool's lazy health-check to skip PINGs on freshly-returned connections.
     * Mutated on the hot path; reads are not synchronised because each connection
     * is owned by a single coroutine at a time.
     */
    @ReThisInternal
    var lastTouchedAt: TimeSource.Monotonic.ValueTimeMark = TimeSource.Monotonic.markNow()

    @ReThisInternal
    @OptIn(InternalAPI::class, InternalIoApi::class)
    suspend fun doRequest(
        payload: Buffer,
        attributesOut: Buffer? = null,
        commandTimeout: Duration? = null,
    ): Buffer {
        COMMON_LOGGER.trace { "Request:\n${payload.copy().readString()}" }

        output.writeBuffer(payload)
        output.flush()

        val response = Buffer()
        readWithTimeout(commandTimeout) {
            input.readCompleteResponseInto(response, attributesOut)
        }

        COMMON_LOGGER.trace { "Response:\n${response.copy().readString()}" }
        return response
    }

    @ReThisInternal
    @OptIn(InternalAPI::class, InternalIoApi::class)
    suspend fun doBatchRequest(
        payload: List<Buffer>,
        commandTimeout: Duration? = null,
    ): Buffer {
        COMMON_LOGGER.trace {
            "Request:\n${payload.joinToString("\n") { it.copy().readString() }}"
        }
        for (request in payload) {
            output.writeBuffer(request)
        }
        output.flush()

        val response = Buffer()
        readWithTimeout(commandTimeout) {
            repeat(payload.size) {
                input.readCompleteResponseInto(response)
            }
        }

        COMMON_LOGGER.trace { "Response:\n${response.copy().readString()}" }
        return response
    }

    private suspend inline fun readWithTimeout(
        timeout: Duration?,
        crossinline block: suspend () -> Unit,
    ) {
        if (timeout == null) {
            block()
            return
        }

        try {
            withTimeout(timeout) { block() }
        } catch (e: TimeoutCancellationException) {
            throw CommandTimeoutException("Command timed out after $timeout", e)
        }
    }
}

internal fun Socket.rConnection(): RConnection =
    RConnection(
        this,
        openReadChannel(),
        openWriteChannel(),
    )
