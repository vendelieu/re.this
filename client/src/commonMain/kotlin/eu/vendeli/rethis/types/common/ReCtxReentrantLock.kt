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
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Local view of the hold. Every coroutine in the hierarchy shares one Redis token, so [epoch] tells one acquisition
 * from the next. A stale `unlock()` issued after the lock was lost and re-adopted can still release the newer
 * hold; this is accepted because the token cannot tell them apart in Redis.
 */
private data class HierarchicalLockState(
    val token: String,
    val depth: Int,
    val leaseMs: Long,
    val epoch: Long,
)

/**
 * Hierarchical distributed reentrant lock with Job-tree-based ownership.
 */
@OptIn(ExperimentalAtomicApi::class, ExperimentalUuidApi::class)
internal class ReCtxReentrantLock(
    private val client: ReThis,
    private val key: String,
    private val referenceJob: Job, // The root of the hierarchy
    private val defaultLeaseMs: Long = 30_000L,
    private val backoffBaseMs: Long = 50L,
) : ReDistributedLock {
    private val channelName = "lock_channel:{$key}"

    // Every coroutine in the hierarchy uses this same token
    private val stableToken = "hier:${Uuid.random()}"

    private val state = AtomicReference<HierarchicalLockState?>(null)
    private val localMutex = Mutex()
    private val watchdogJob = AtomicReference<Job?>(null)
    private val epochCounter = AtomicLong(0)

    override suspend fun lock(leaseTime: Duration) {
        tryLock(Duration.INFINITE, leaseTime)
    }

    override suspend fun tryLock(waitTime: Duration, leaseTime: Duration): Boolean {
        val currentJob = currentCoroutineContext()[Job] ?: error("No Job in context")

        // Ensure the caller is part of the authorized tree
        if (!currentJob.isRelativeTo(referenceJob)) {
            throw IllegalStateException("Lock accessed outside its hierarchy for key=$key")
        }

        val leaseMs = if (leaseTime.isPositive()) leaseTime.inWholeMilliseconds else defaultLeaseMs
        val timeoutMark = TimeSource.Monotonic.markNow() + waitTime

        // 1. Fast-path: Hierarchical Reentrancy (Already held by someone in the tree)
        val currentState = state.load()
        if (currentState != null) {
            val watchdog = watchdogJob.load()
            // Re-verify with Redis to ensure TTL hasn't wiped the lock
            if (acquireScript(stableToken, leaseMs) == 1) {
                if (!incrementDepth()) {
                    // Redis already counted this acquire, so the adoption must not be abandoned half way.
                    withContext(NonCancellable) { localMutex.withLock { adoptHeldLocally(leaseMs) } }
                }
                return true
            }
            // If failed, the Redis lock was lost/expired; clear local state
            if (state.compareAndSet(currentState, null)) cancelWatchdog(watchdog)
        }

        // 2. Local Serialization
        localMutex.withLock {
            // Double-check after acquiring mutex
            val recheck = state.load()
            if (recheck != null) {
                if (acquireScript(stableToken, leaseMs) == 1) {
                    if (!incrementDepth()) adoptHeldLocally(leaseMs)
                    return true
                }
            }

            val waiter = LockReleaseWaiter(client, channelName)
            var isSubscribed = false

            try {
                while (true) {
                    currentCoroutineContext().ensureActive()

                    when (acquireScript(stableToken, leaseMs)) {
                        1 -> {
                            val acquired =
                                HierarchicalLockState(stableToken, 1, leaseMs, epochCounter.addAndFetch(1L))
                            state.store(acquired)
                            startWatchdog(stableToken, leaseMs, acquired.epoch)
                            return true
                        }

                        0 -> { // Held by a different process/hierarchy
                        }

                        else -> {
                            throw LockLostException("Corrupted state for key=$key")
                        }
                    }

                    val remaining = timeoutMark - TimeSource.Monotonic.markNow()
                    if (remaining <= Duration.ZERO) return false

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

        return when (val result = releaseScript(stableToken, loaded.leaseMs)) {
            1 -> {
                decrementDepth(loaded.epoch, watchdog)
                true
            }

            0 -> {
                forgetLostLock(loaded, watchdog)
                throw LockLostException("Lock expired in Redis")
            }

            -1 -> {
                forgetLostLock(loaded, watchdog)
                throw LockLostException("Token mismatch in Redis for key=$key")
            }

            else -> {
                throw IllegalStateException("Unexpected Redis response: $result")
            }
        }
    }

    // Siblings in the hierarchy run these concurrently, so each change is applied to the freshest state.
    // Returns false when the state is gone, i.e. the local hold was dropped after Redis already counted the acquire.
    private fun incrementDepth(): Boolean {
        while (true) {
            val current = state.load() ?: return false
            if (state.compareAndSet(current, current.copy(depth = current.depth + 1))) return true
        }
    }

    // Only the hold of [epoch] is touched; a newer acquisition's state and watchdog stay as they are.
    private fun decrementDepth(epoch: Long, watchdog: Job?) {
        while (true) {
            val current = state.load() ?: return
            if (current.epoch != epoch) return
            val next = if (current.depth <= 1) null else current.copy(depth = current.depth - 1)
            if (state.compareAndSet(current, next)) {
                if (next == null) cancelWatchdog(watchdog)
                return
            }
        }
    }

    // Redis counted an acquire but the local state vanished meanwhile: treat it as the first hold.
    private fun adoptHeldLocally(leaseMs: Long) {
        if (incrementDepth()) return
        val adopted = HierarchicalLockState(stableToken, 1, leaseMs, epochCounter.addAndFetch(1L))
        state.store(adopted)
        startWatchdog(stableToken, leaseMs, adopted.epoch)
    }

    // The lock is gone from Redis, so keeping the local state and its watchdog would only refresh nothing forever.
    private fun forgetLostLock(lostState: HierarchicalLockState, watchdog: Job?) {
        if (state.compareAndSet(lostState, null)) cancelWatchdog(watchdog)
    }

    // Clears only the acquisition this watchdog was started for; a newer owner's state must survive a late result.
    private fun forgetLostHold(epoch: Long) {
        while (true) {
            val current = state.load() ?: return
            if (current.epoch != epoch || state.compareAndSet(current, null)) return
        }
    }

    private fun cancelWatchdog(watchdog: Job?) {
        watchdog?.cancel()
        watchdogJob.compareAndSet(watchdog, null)
    }

    private fun startWatchdog(token: String, leaseMs: Long, epoch: Long) {
        stopWatchdog()
        val renewalInterval = (leaseMs / 3).coerceAtLeast(1L)
        // The watchdog is deliberately not a child of the referenceJob: a child would keep the referenceJob from
        // completing until the watchdog ends, and a failing child would cancel it. Instead it is cancelled
        // when the referenceJob completes.
        val watchdog = CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
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
                    forgetLostHold(epoch)
                    cancel("Watchdog lost lock")
                }
            }
        }
        val referenceHandle = referenceJob.invokeOnCompletion { watchdog.cancel() }
        watchdog.invokeOnCompletion { referenceHandle.dispose() }
        watchdogJob.store(watchdog)
    }

    private fun stopWatchdog() = watchdogJob.exchange(null)?.cancel()

    // -------------------------------------------------------------------------
    // Script Wrappers (Shared with the other implementation)
    // -------------------------------------------------------------------------

    private suspend fun acquireScript(token: String, lease: Long) =
        client.evalAsInt(ACQUIRE_LUA, arrayOf(key), listOf(token, lease.toString()))

    private suspend fun releaseScript(token: String, lease: Long) =
        client.evalAsInt(RELEASE_LUA, arrayOf(key), listOf(token, lease.toString(), channelName))

    private suspend fun refreshScript(token: String, lease: Long) =
        client.evalAsInt(REFRESH_LUA, arrayOf(key), listOf(token, lease.toString()))
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun Job.isRelativeTo(referenceJob: Job): Boolean {
    if (this === referenceJob) return true
    var current: Job? = this
    while (current != null) {
        if (current === referenceJob) return true
        current = current.parent
    }
    return false
}
