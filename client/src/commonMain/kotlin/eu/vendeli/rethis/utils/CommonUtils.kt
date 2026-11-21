@file:Suppress("KotlinRedundantDiagnosticSuppress")

package eu.vendeli.rethis.utils

import eu.vendeli.rethis.configuration.ReThisConfiguration
import eu.vendeli.rethis.shared.response.common.HostAndPort
import eu.vendeli.rethis.topology.ClusterNotReadyException
import eu.vendeli.rethis.shared.types.*
import eu.vendeli.rethis.types.common.Address
import eu.vendeli.rethis.types.coroutine.CoLocalConn
import io.ktor.network.sockets.*
import io.ktor.util.logging.*
import kotlinx.coroutines.*
import kotlinx.io.IOException

internal val COMMON_LOGGER = KtorSimpleLogger("eu.vendeli.rethis.ReThisCommonLogger")

expect val Dispatchers.IO_OR_UNCONFINED: CoroutineDispatcher

internal const val BLOCKING_UNSUPPORTED_MESSAGE =
    "Blocking calls are not available on JS and wasmJs; use the suspend API instead."

/**
 * Thrown by [coRunBlocking] on targets that cannot block. It is an [UnsupportedOperationException] for callers,
 * and lets internal fallbacks tell "cannot block here" apart from an unsupported operation raised by the block itself.
 */
internal class BlockingUnsupportedException : UnsupportedOperationException(BLOCKING_UNSUPPORTED_MESSAGE)

/**
 * Runs [block] and blocks the current thread until it completes.
 *
 * Not available on JS and wasmJs, where it throws [UnsupportedOperationException].
 */
expect fun <T> coRunBlocking(block: suspend CoroutineScope.() -> T): T

internal suspend inline fun <T> withRetry(
    cfg: ReThisConfiguration,
    isRetryable: (Throwable) -> Boolean = Throwable::isTransientFailure,
    block: suspend (attempt: Int) -> T,
): T {
    val logger = cfg.loggerFactory.get("eu.vendeli.rethis.ReThis")
    var currentDelay = cfg.retry.initialDelay.inWholeMilliseconds
    var ex: Exception? = null
    val totalAttempts = cfg.retry.times.coerceAtLeast(1)
    for (attempt in 0 until totalAttempts) {
        logger.trace { "Attempt ${attempt + 1} of $totalAttempts" }
        try {
            return block(attempt)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.debug("Caught exception at ${attempt + 1} attempt", e)
            if (ex == null) {
                ex = e
            } else {
                ex.addSuppressed(e)
            }
            if (!isRetryable(e)) break
        }
        // no backoff after the final attempt, just surface the failure
        if (attempt == totalAttempts - 1) break
        delay(currentDelay)
        currentDelay = (currentDelay * cfg.retry.factor).toLong().coerceAtMost(cfg.retry.maxDelay.inWholeMilliseconds)
    }
    throw ex!!
}

internal inline fun HostAndPort.toAddress(): Address = Address(host, port)
internal inline fun Address.toHostAndPort(): HostAndPort? =
    if (socket is InetSocketAddress) HostAndPort(socket.hostname, socket.port) else null

internal suspend fun isInTx(): Boolean {
    val coLocalCon = currentCoroutineContext()[CoLocalConn]
    return coLocalCon != null && coLocalCon.isTx
}

/**
 * Failures worth another attempt regardless of the command: connection and transport errors, plus cluster
 * redirects and unstable/down cluster states that the next attempt resolves after the topology is updated.
 * A cluster whose topology is not known yet ([ClusterNotReadyException]) is transient too; the other cluster errors
 * (no master for a slot, CROSSSLOT, absent key), Redis error replies, parse errors and other [ReThisException]s are
 * permanent.
 */
internal fun Throwable.isTransientFailure(): Boolean = when (this) {
    is IOException,
    is RespUnexpectedEOF,
    is RedirectAskException,
    is RedirectMovedException,
    is RedirectUnstableException,
    is DownUnboundSlotException,
    is DownReadOnlyStateException,
    is ClusterNotReadyException,
    -> true

    else -> false
}

/** A timed-out command may have been applied, so it is re-sent only when it cannot change data. */
internal fun Throwable.isRetryableFor(operation: RedisOperation): Boolean =
    isTransientFailure() || (this is CommandTimeoutException && operation == RedisOperation.READ)
