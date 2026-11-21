package eu.vendeli.rethis.core

import eu.vendeli.rethis.shared.types.CommandTimeoutException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

/**
 * Runs [block] for at most [timeout] while acquiring a connection. When only that bound expires, the failure is a
 * [CommandTimeoutException] (a regular failure, not a cancellation) so callers and `withRetry` treat it like a command
 * timeout: reads are retried, writes are not. A timeout or cancellation coming from the caller is rethrown as is.
 */
internal suspend fun <T> withAcquireTimeout(timeout: Duration, block: suspend CoroutineScope.() -> T): T = try {
    withTimeout(timeout, block)
} catch (e: TimeoutCancellationException) {
    currentCoroutineContext().ensureActive()
    throw CommandTimeoutException("Timed out after $timeout waiting for a connection", e)
}
