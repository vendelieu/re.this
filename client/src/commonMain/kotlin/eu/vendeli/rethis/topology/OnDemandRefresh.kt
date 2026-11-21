package eu.vendeli.rethis.topology

import eu.vendeli.rethis.shared.types.ClusterException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.concurrent.Volatile
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * No cluster topology is known yet (startup, or a failed on-demand fetch during a failover). Unlike the other cluster
 * errors it is transient, so `withRetry` retries it.
 */
internal class ClusterNotReadyException(
    cause: Throwable? = null,
) : ClusterException("Cluster not ready", cause)

internal val ON_DEMAND_REFRESH_COOL_DOWN: Duration = 1.seconds

/**
 * Lets concurrent callers share one topology refresh while no topology is known yet: callers queue on [mutex],
 * re-check the topology once they hold it. After a failed refresh, callers already queued behind it fail fast with
 * the same cause, while a caller arriving during the cool-down first waits out its remainder (once, in parallel with
 * the others) and then takes part in the next refresh, so a retry spaced by the retry backoff can outlast a short
 * failover window instead of burning every attempt on the cached failure.
 */
internal class OnDemandRefresh(
    private val mutex: Mutex,
    private val coolDown: Duration = ON_DEMAND_REFRESH_COOL_DOWN,
    private val timeSource: TimeSource = TimeSource.Monotonic,
) {
    @Volatile
    private var lastFailure: Pair<TimeMark, Exception>? = null

    private suspend fun waitOutCoolDown() {
        val (at, _) = lastFailure ?: return
        val remaining = coolDown - at.elapsedNow()
        if (remaining.isPositive()) delay(remaining)
    }

    private fun failFastDuringCoolDown() {
        val (at, cause) = lastFailure ?: return
        if (at.elapsedNow() < coolDown) throw ClusterNotReadyException(cause)
    }

    suspend fun <T : Any> ensure(current: () -> T?, refresh: suspend () -> Unit): T {
        current()?.let { return it }
        waitOutCoolDown()
        return mutex.withLock {
            current()?.let { return@withLock it }
            failFastDuringCoolDown()
            val failure = try {
                refresh()
                null
            } catch (e: TimeoutCancellationException) {
                // a bounded timeout inside the refresh is a failed refresh; cancellation of the caller is not
                currentCoroutineContext().ensureActive()
                e
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e
            }
            lastFailure = failure?.let { timeSource.markNow() to it }
            current() ?: throw ClusterNotReadyException(failure)
        }
    }
}
