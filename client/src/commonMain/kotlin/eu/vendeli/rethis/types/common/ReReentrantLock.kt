package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.types.LockLostException
import eu.vendeli.rethis.types.interfaces.ReDistributedLock
import eu.vendeli.rethis.utils.LockUtils.ACQUIRE_LUA
import eu.vendeli.rethis.utils.LockUtils.REFRESH_LUA
import eu.vendeli.rethis.utils.LockUtils.RELEASE_LUA
import eu.vendeli.rethis.utils.evalAsInt
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Internal state representing the local ownership of the lock.
 * * @param ownerJob The specific Coroutine Job that acquired the lock.
 * @param token The unique string identifier stored in Redis to verify ownership.
 * @param depth The reentrancy level (how many times the same Job called lock()).
 * @param leaseMs The lease the lock was acquired with, reused when a nested unlock renews the TTL.
 */
private data class LockState(
    val ownerJob: Job,
    val token: String,
    val depth: Int,
    val leaseMs: Long,
)

@OptIn(ExperimentalAtomicApi::class, ExperimentalUuidApi::class)
internal class ReReentrantLock(
    private val client: ReThis,
    private val key: String,
    private val defaultLeaseMs: Long = 30_000L,
    private val backoffBaseMs: Long = 50L,
) : ReDistributedLock {
    private val channelName = "lock_channel:{$key}"
    private val instanceId = Uuid.random().toString()
    private val state = AtomicReference<LockState?>(null)
    private val localMutex = Mutex()
    private val watchdogJob = AtomicReference<Job?>(null)
    private val watchdogScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Standard lock: waits indefinitely (or until coroutine cancellation)
     * to acquire the lock.
     */
    override suspend fun lock(leaseTime: Duration) {
        val lease = if (leaseTime.isPositive()) leaseTime else defaultLeaseMs.milliseconds
        // tryLock with INFINITE handles the Pub/Sub waiting and cancellation internally
        tryLock(Duration.INFINITE, lease)
    }

    override suspend fun tryLock(waitTime: Duration, leaseTime: Duration): Boolean {
        val currentJob = currentCoroutineContext()[Job] ?: error("No Job in context")
        val leaseMs = if (leaseTime.isPositive()) leaseTime.inWholeMilliseconds else defaultLeaseMs
        val timeoutMark = TimeSource.Monotonic.markNow() + waitTime

        // 1. Fast-path: Local Reentrancy
        val currentState = state.load()
        if (currentState?.ownerJob === currentJob) {
            return if (acquireScript(currentState.token, leaseMs) == 1) {
                state.store(currentState.copy(depth = currentState.depth + 1))
                true
            } else {
                throw LockLostException("Lock state mismatch in Redis for $key")
            }
        }

        // 2. Local Serialization
        // We don't 'return' the withLock; we let the withLock execute returns directly
        localMutex.withLock {
            // Re-check state after acquiring mutex (double-check locking)
            val stateAfterMutex = state.load()
            if (stateAfterMutex?.ownerJob === currentJob) {
                if (acquireScript(stateAfterMutex.token, leaseMs) == 1) {
                    state.store(stateAfterMutex.copy(depth = stateAfterMutex.depth + 1))
                    return true // Non-local return: exits tryLock entirely
                }
            }

            val token = "$instanceId:${Uuid.random()}"
            val waiter = LockReleaseWaiter(client, channelName)
            var isSubscribed = false

            try {
                while (true) {
                    currentCoroutineContext().ensureActive()

                    when (acquireScript(token, leaseMs)) {
                        1 -> {
                            state.store(LockState(currentJob, token, 1, leaseMs))
                            startWatchdog(token, leaseMs)
                            return true // Exits tryLock with success
                        }

                        0 -> { // Keep trying
                        }

                        else -> {
                            throw LockLostException("Corrupted lock state in Redis for $key")
                        }
                    }

                    val remaining = timeoutMark - TimeSource.Monotonic.markNow()
                    if (remaining <= Duration.ZERO) {
                        return false // Exits tryLock with failure
                    }

                    val pause = minOf(remaining, backoffBaseMs.coerceAtLeast(1L).milliseconds)
                    if (isSubscribed) {
                        waiter.await(pause)
                    } else {
                        // Subscribe once, then retry the acquire right away to narrow the window in which a
                        // release goes unnoticed. open() returns when the worker is launched, not when the
                        // server processed SUBSCRIBE; a release in that gap is recovered by the bounded wait.
                        waiter.open()
                        isSubscribed = true
                    }
                }
            } finally {
                waiter.close()
            }
        }
    }

    override suspend fun unlock(): Boolean {
        val loaded = state.load() ?: throw LockLostException("Lock not held locally")
        // Captured before the release: once the key is free another coroutine may acquire and start its own watchdog.
        val watchdog = watchdogJob.load()

        return when (val result = releaseScript(loaded.token, loaded.leaseMs)) {
            1 -> {
                decrementDepth(loaded.token, watchdog)
                true
            }

            0 -> {
                forgetLostLock(loaded, watchdog)
                throw LockLostException("Lock expired in Redis")
            }

            -1 -> {
                forgetLostLock(loaded, watchdog)
                throw LockLostException("Attempted to unlock a lock owned by another process")
            }

            else -> {
                throw IllegalStateException("Unexpected Redis response: $result")
            }
        }
    }

    // Only the hold identified by [token] is touched; a newer owner's state and watchdog stay as they are.
    private fun decrementDepth(token: String, watchdog: Job?) {
        while (true) {
            val current = state.load() ?: return
            if (current.token != token) return
            val next = if (current.depth <= 1) null else current.copy(depth = current.depth - 1)
            if (state.compareAndSet(current, next)) {
                if (next == null) cancelWatchdog(watchdog)
                return
            }
        }
    }

    // The lock is gone from Redis, so keeping the local state and its watchdog would only refresh nothing forever.
    private fun forgetLostLock(lostState: LockState, watchdog: Job?) {
        if (state.compareAndSet(lostState, null)) cancelWatchdog(watchdog)
    }

    // Clears only the hold this watchdog was started for; a newer owner's state must survive a late refresh result.
    private fun forgetLostHold(token: String) {
        while (true) {
            val current = state.load() ?: return
            if (current.token != token || state.compareAndSet(current, null)) return
        }
    }

    private fun cancelWatchdog(watchdog: Job?) {
        watchdog?.cancel()
        watchdogJob.compareAndSet(watchdog, null)
    }

    private fun startWatchdog(token: String, leaseMs: Long) {
        stopWatchdog()
        val renewalInterval = (leaseMs / 3).coerceAtLeast(1L)
        watchdogJob.store(
            watchdogScope.launch {
                while (isActive) {
                    delay(renewalInterval)
                    val isRefreshed = try {
                        refreshScript(token, leaseMs) == 1
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        // A transient failure must not end the watchdog; the next tick retries.
                        client.logger.warn("Lock lease refresh failed for $key, retrying", e)
                        continue
                    }
                    if (!isRefreshed) {
                        forgetLostHold(token)
                        cancel("Lock lost in Redis")
                    }
                }
            },
        )
    }

    private fun stopWatchdog() = watchdogJob.exchange(null)?.cancel()

    // -------------------------------------------------------------------------
    // Optimized Lua Scripts
    // -------------------------------------------------------------------------

    private suspend fun acquireScript(token: String, lease: Long): Int =
        client.evalAsInt(ACQUIRE_LUA, arrayOf(key), listOf(token, lease.toString()))

    private suspend fun releaseScript(token: String, lease: Long): Int =
        client.evalAsInt(RELEASE_LUA, arrayOf(key), listOf(token, lease.toString(), channelName))

    private suspend fun refreshScript(token: String, lease: Long): Int =
        client.evalAsInt(REFRESH_LUA, arrayOf(key), listOf(token, lease.toString()))
}
