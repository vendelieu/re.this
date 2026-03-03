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
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Internal state representing the local ownership of the lock.
 * * @param ownerJob The specific Coroutine Job that acquired the lock.
 * @param token The unique string identifier stored in Redis to verify ownership.
 * @param depth The reentrancy level (how many times the same Job called lock()).
 */
private data class LockState(
    val ownerJob: Job,
    val token: String,
    val depth: Int,
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

            while (true) {
                currentCoroutineContext().ensureActive()

                when (acquireScript(token, leaseMs)) {
                    1 -> {
                        state.store(LockState(currentJob, token, 1))
                        startWatchdog(token, leaseMs)
                        return true // Exits tryLock with success
                    }

                    0 -> { /* Keep trying */
                    }

                    else -> throw LockLostException("Corrupted lock state in Redis for $key")
                }

                val remaining = timeoutMark - TimeSource.Monotonic.markNow()
                if (remaining <= Duration.ZERO) {
                    return false // Exits tryLock with failure
                }

                // Wait for Pub/Sub signal or a small fallback delay
                waitForNotification(remaining)
            }
        }
    }

    override suspend fun unlock(): Boolean {
        val currentState = state.load() ?: throw LockLostException("Lock not held locally")
        val isFinal = currentState.depth == 1

        return when (val result = releaseScript(currentState.token, defaultLeaseMs)) {
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
            -1 -> throw LockLostException("Attempted to unlock a lock owned by another process")
            else -> throw IllegalStateException("Unexpected Redis response: $result")
        }
    }

    private suspend fun waitForNotification(timeout: Duration) {
        withTimeoutOrNull(timeout) {
            client.subscribe(channelName) { rc, _: Buffer ->
                rc.unsubscribe(channelName)
            }
        } ?: delay(backoffBaseMs) // Fallback if Pub/Sub fails/times out
    }

    private fun startWatchdog(token: String, leaseMs: Long) {
        stopWatchdog()
        val renewalInterval = leaseMs / 3
        watchdogJob.store(
            watchdogScope.launch {
                while (isActive) {
                    delay(renewalInterval)
                    if (refreshScript(token, leaseMs) != 1) {
                        this@ReReentrantLock.state.store(null)
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
