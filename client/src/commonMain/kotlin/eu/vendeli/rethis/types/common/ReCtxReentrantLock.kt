package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.pubsub.subscribe
import eu.vendeli.rethis.command.pubsub.unsubscribe
import eu.vendeli.rethis.shared.types.LockLostException
import eu.vendeli.rethis.types.interfaces.ReDistributedLock
import eu.vendeli.rethis.utils.LockUtils.ACQUIRE_LUA
import eu.vendeli.rethis.utils.LockUtils.REFRESH_LUA
import eu.vendeli.rethis.utils.LockUtils.RELEASE_LUA
import eu.vendeli.rethis.utils.evalAsInt
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.Buffer
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration
import kotlin.time.TimeSource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Hierarchical distributed reentrant lock with Job-tree-based ownership.
 */

private data class HierarchicalLockState(
    val token: String,
    val depth: Int,
)

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

    // Watchdog is bound to the referenceJob so it dies if the hierarchy dies
    private val watchdogScope = CoroutineScope(referenceJob + Dispatchers.Default)

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
            // Re-verify with Redis to ensure TTL hasn't wiped the lock
            if (acquireScript(stableToken, leaseMs) == 1) {
                state.store(currentState.copy(depth = currentState.depth + 1))
                return true
            }
            // If failed, the Redis lock was lost/expired; clear local state
            state.store(null)
            stopWatchdog()
        }

        // 2. Local Serialization
        localMutex.withLock {
            // Double-check after acquiring mutex
            val recheck = state.load()
            if (recheck != null) {
                if (acquireScript(stableToken, leaseMs) == 1) {
                    state.store(recheck.copy(depth = recheck.depth + 1))
                    return true
                }
            }

            while (true) {
                currentCoroutineContext().ensureActive()

                when (acquireScript(stableToken, leaseMs)) {
                    1 -> {
                        state.store(HierarchicalLockState(stableToken, 1))
                        startWatchdog(stableToken, leaseMs)
                        return true
                    }

                    0 -> { /* Held by a different process/hierarchy */
                    }

                    else -> throw LockLostException("Corrupted state for key=$key")
                }

                val remaining = timeoutMark - TimeSource.Monotonic.markNow()
                if (remaining <= Duration.ZERO) return false

                // Use the same Pub/Sub wait logic
                waitForNotification(remaining)
            }
        }
    }

    override suspend fun unlock(): Boolean {
        val currentState = state.load() ?: throw LockLostException("Lock not held locally")
        val isFinal = currentState.depth == 1

        return when (val result = releaseScript(stableToken, defaultLeaseMs)) {
            1 -> {
                if (isFinal) {
                    stopWatchdog()
                    state.store(null)
                } else {
                    state.store(currentState.copy(depth = currentState.depth - 1))
                }
                true
            }

            0 -> throw LockLostException("Lock expired in Redis")
            -1 -> throw LockLostException("Token mismatch in Redis for key=$key")
            else -> throw IllegalStateException("Unexpected Redis response: $result")
        }
    }

    private suspend fun waitForNotification(timeout: Duration) {
        withTimeoutOrNull(timeout) {
            client.subscribe(channelName) { rc, _: Buffer ->
                rc.unsubscribe(channelName)
            }
        } ?: delay(backoffBaseMs)
    }

    private fun startWatchdog(token: String, leaseMs: Long) {
        stopWatchdog()
        val renewalInterval = leaseMs / 3
        watchdogJob.store(
            watchdogScope.launch {
                while (isActive) {
                    delay(renewalInterval)
                    if (refreshScript(token, leaseMs) != 1) {
                        state.store(null)
                        cancel("Watchdog lost lock")
                    }
                }
            },
        )
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
