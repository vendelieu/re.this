package eu.vendeli.rethis.topology

import eu.vendeli.rethis.core.ConnectionFactory
import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.types.common.RConnection
import io.ktor.network.sockets.SocketAddress
import io.ktor.util.logging.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.io.IOException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Picks the seed for a zero-based retry [attempt], cycling through the list without consuming it. */
internal fun <T> List<T>.seedFor(attempt: Int): T = this[attempt % size]

/** Opens a connection for a topology fetch; an unavailable one is a transport failure so the fetch is retried. */
internal suspend fun ConnectionFactory.createConnOrIoError(address: SocketAddress): RConnection =
    createConnOrNull(address) ?: throw IOException("Can't create connection to $address")

/**
 * Runs a topology refresh, logging and absorbing any failure other than cancellation (a bounded timeout counts as a failure) so the owning
 * scope and its periodic loop stay alive. Returns whether the refresh succeeded.
 */
internal suspend inline fun runRefreshSafely(logger: Logger, refresh: () -> Unit): Boolean = try {
    refresh()
    true
} catch (e: TimeoutCancellationException) {
    logger.warn("Topology refresh timed out", e)
    false
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    logger.warn("Topology refresh failed", e)
    false
}

/** Upper bound for one latency probe, so a half-dead node cannot stall a refresh when no command timeout is set. */
internal val LATENCY_PROBE_TIMEOUT: Duration = 2.seconds

/**
 * Probes every provider and returns its latency by index. A probe that fails or takes longer than
 * [LATENCY_PROBE_TIMEOUT] is logged and recorded as [Duration.INFINITE] so it never wins a lowest-latency pick and
 * never aborts the refresh.
 */
internal suspend fun measureLatencies(
    providers: Array<ConnectionProvider>,
    logger: Logger,
    probe: suspend (ConnectionProvider) -> Duration,
): Map<Int, Duration> = providers.withIndex().associate { (idx, provider) ->
    idx to try {
        withTimeout(LATENCY_PROBE_TIMEOUT) { probe(provider) }
    } catch (e: TimeoutCancellationException) {
        logger.warn("Latency probe timed out for ${provider.node}", e)
        Duration.INFINITE
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        logger.warn("Latency probe failed for ${provider.node}", e)
        Duration.INFINITE
    }
}
