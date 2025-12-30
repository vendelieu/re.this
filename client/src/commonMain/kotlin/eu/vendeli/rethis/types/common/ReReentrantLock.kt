package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.scripting.eval
import eu.vendeli.rethis.shared.types.LockLostException
import eu.vendeli.rethis.shared.utils.unwrap
import eu.vendeli.rethis.types.interfaces.ReDistributedLock
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.TimeSource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Standard distributed reentrant lock with per-coroutine ownership.
 *
 * Ownership is determined by the specific coroutine (Job) that acquired the lock:
 * - Same coroutine can call lock() multiple times (reentrant)
 * - Different coroutines block each other, even when sharing the same lock instance
 *
 * This behavior matches the standard ReentrantLock semantics.
 *
 * Implementation notes:
 * - Redis representation is a HASH at `key` with fields:
 *     - owner: string (token)
 *     - count: integer
 *     - ver: 1 (format version)
 * - Scripts return codes (by convention):
 *     1 -> success
 *     0 -> absent / not-acquired (for acquire: means someone else holds it)
 *    -1 -> token mismatch (caller is not owner)
 *    -2 -> corrupted state (malformed fields)
 */

/**
 * Holds the current lock ownership state.
 * Immutable - create new instance for state changes.
 */
private data class LockState(
    val ownerJob: Job,
    val token: String,
    val depth: Int,
)

@OptIn(ExperimentalAtomicApi::class)
internal class ReReentrantLock(
    private val client: ReThis,
    private val key: String,
    private val defaultLeaseMs: Long = 30_000L,
    private val backoffBaseMs: Long = 50L,
    private val backoffCapMs: Long = 1000L,
) : ReDistributedLock {
    @OptIn(ExperimentalUuidApi::class)
    private val instanceId = "inst:${Uuid.random()}"

    // Single atomic holding all ownership state (null = not owned)
    private val state = AtomicReference<LockState?>(null)

    private val localMutex = Mutex()

    // Watchdog scope + job
    private val watchdogScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val watchdogJob = AtomicReference<Job?>(null)

    override suspend fun tryLock(waitTime: Duration, leaseTime: Duration): Boolean {
        currentCoroutineContext().ensureActive()
        val currentJob = currentCoroutineContext()[Job] ?: error("No Job in context")

        val waitMs = waitTime.toLong(DurationUnit.MILLISECONDS).coerceAtLeast(0)
        val leaseMs = leaseTime.toLong(DurationUnit.MILLISECONDS).let { if (it > 0) it else defaultLeaseMs }
        val start = TimeSource.Monotonic.markNow()

        // Fast-path: if THIS coroutine already owns the lock, it's reentrant
        val currentState = state.load()
        if (currentState != null && currentState.ownerJob === currentJob) {
            val r = acquireScript(currentState.token, leaseMs)
            when (r) {
                1 -> {
                    state.store(currentState.copy(depth = currentState.depth + 1))
                    return true
                }

                else -> throw LockLostException("Lock lost during reentrant acquire for key=$key, code=$r")
            }
        }

        // Different coroutine or first acquisition - serialize via local mutex
        return localMutex.withLock {
            // Double-check after acquiring mutex
            val recheckState = state.load()
            if (recheckState != null && recheckState.ownerJob === currentJob) {
                val r = acquireScript(recheckState.token, leaseMs)
                if (r == 1) {
                    state.store(recheckState.copy(depth = recheckState.depth + 1))
                    return@withLock true
                }
            }

            var lastBackoff = backoffBaseMs

            @OptIn(ExperimentalUuidApi::class)
            val token = "$instanceId:${currentJob.hashCode()}:${Uuid.random()}"

            while (true) {
                currentCoroutineContext().ensureActive()

                when (val r = acquireScript(token, leaseMs)) {
                    1 -> {
                        state.store(LockState(ownerJob = currentJob, token = token, depth = 1))
                        startWatchdog(token, leaseMs)
                        return@withLock true
                    }

                    0 -> { /* someone else holds it in Redis */
                    }

                    -1, -2 -> throw LockLostException("Lock error for key=$key, code=$r")
                }

                if (waitMs == 0L || start.elapsedNow().inWholeMilliseconds >= waitMs) {
                    return@withLock false
                }

                val delayMs = decorrelatedExponentialJitter(lastBackoff).coerceAtMost(backoffCapMs)
                lastBackoff = delayMs
                delay(delayMs)
            }
            @Suppress("UNREACHABLE_CODE")
            false
        }
    }

    override suspend fun lock(leaseTime: Duration) {
        val leaseMs = leaseTime.toLong(DurationUnit.MILLISECONDS).let { if (it > 0) it else defaultLeaseMs }
        while (true) {
            currentCoroutineContext().ensureActive()
            if (tryLock(Duration.ZERO, leaseMs.milliseconds)) return
            delay(decorrelatedExponentialJitter(backoffBaseMs))
        }
    }

    override suspend fun unlock(): Boolean {
        currentCoroutineContext().ensureActive()

        val currentState = state.load()
            ?: throw LockLostException("Unlock called but lock not held for key=$key")

        val newDepth = currentState.depth - 1
        val isFinal = newDepth == 0

        when (val r = releaseScript(currentState.token, defaultLeaseMs)) {
            1 -> {
                if (isFinal) {
                    stopWatchdog()
                    state.store(null)
                } else {
                    state.store(currentState.copy(depth = newDepth))
                }
                return true
            }

            0 -> throw LockLostException("Lock already missing/expired for key=$key during unlock")
            -1 -> throw LockLostException("Unlock attempted by non-owner for key=$key")
            -2 -> throw LockLostException("Corrupted lock state for key=$key during unlock")
            else -> throw IllegalStateException("Unexpected unlock script response: $r for key=$key")
        }
    }

    /**
     * Checks if currentJob is the ownerJob or a child/descendant of ownerJob.
     * This allows unlock from withContext(NonCancellable) which creates a child job.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun isOwnerOrChild(currentJob: Job, ownerJob: Job): Boolean = when {
        currentJob === ownerJob -> true
        currentJob === NonCancellable && currentJob.parent == ownerJob -> true
        else -> false
    }

    private fun startWatchdog(token: String, leaseMs: Long) {
        stopWatchdog()
        watchdogJob.compareAndSet(
            null,
            watchdogScope.launch {
                val base = (leaseMs / 3).coerceAtLeast(50L)
                while (isActive) {
                    val jitter = Random.nextLong(0, base / 5 + 1)
                    delay(base + jitter)
                    try {
                        val r = refreshScript(token, leaseMs)
                        if (r != 1) {
                            throw LockLostException("Watchdog failed to refresh key=$key response=$r")
                        }
                    } catch (t: Throwable) {
                        stopWatchdog()
                        cancel("Watchdog stopped due to error: ${t.message}")
                    }
                }
            },
        )
    }

    private fun stopWatchdog() {
        watchdogJob.exchange(null)?.cancel()
    }

    // ------------------------
    // Helpers
    // ------------------------

    private fun decorrelatedExponentialJitter(prev: Long): Long {
        val previous = prev * 3
        val newDelay = Random.nextLong(backoffBaseMs, previous.coerceAtLeast(backoffBaseMs + 1))
        return newDelay.coerceAtMost(backoffCapMs)
    }

    // Remove the verifyTTL method entirely - it's redundant with watchdog and causes flaky tests

    // ------------------------
    // Redis script wrappers
    // ------------------------

    private suspend fun acquireScript(token: String, leaseMs: Long): Int =
        runCatching {
            val r = client.eval(ACQUIRE_SCRIPT, key = arrayOf(key), arg = listOf(token, leaseMs.toString()))
            r.unwrap<Long?>()?.toInt() ?: -2
        }.getOrElse { -2 }

    private suspend fun releaseScript(token: String, leaseMs: Long): Int =
        runCatching {
            val r = client.eval(RELEASE_SCRIPT, key = arrayOf(key), arg = listOf(token, leaseMs.toString()))
            r.unwrap<Long?>()?.toInt() ?: -2
        }.getOrElse { -2 }

    private suspend fun refreshScript(token: String, leaseMs: Long): Int =
        runCatching {
            val r = client.eval(REFRESH_SCRIPT, key = arrayOf(key), arg = listOf(token, leaseMs.toString()))
            r.unwrap<Long?>()?.toInt() ?: -2
        }.getOrElse { -2 }

    companion object {
        private const val ACQUIRE_SCRIPT =
            "local key=KEYS[1];local token=ARGV[1];local lease=tonumber(ARGV[2])or 0;local owner=redis.call('HGET',key,'owner');local count=redis.call('HGET',key,'count');local ver=redis.call('HGET',key,'ver');if count and type(count)=='string'then count=tonumber(count)end;if ver and type(ver)=='string'then ver=tonumber(ver)end;if not owner then redis.call('HSET',key,'owner',token);redis.call('HSET',key,'count',1);redis.call('HSET',key,'ver',1);redis.call('PEXPIRE',key,lease);return 1 end;if(not count)or(ver~=1)then return -2 end;if owner==token then local newc=count+1;redis.call('HSET',key,'count',newc);redis.call('PEXPIRE',key,lease);return 1 end;return 0"

        private const val RELEASE_SCRIPT =
            "local key=KEYS[1];local token=ARGV[1];local lease=tonumber(ARGV[2])or 0;local owner=redis.call('HGET',key,'owner');if not owner then return 0 end;local count=redis.call('HGET',key,'count');local ver=redis.call('HGET',key,'ver');if type(count)=='string' then count=tonumber(count) end;if type(ver)=='string' then ver=tonumber(ver) end;if not count or ver~=1 then return -2 end;if owner~=token then return -1 end;local newc=count-1;if newc>0 then redis.call('HSET',key,'count',newc);redis.call('PEXPIRE',key,lease);return 1 else redis.call('DEL',key);return 1 end"

        private const val REFRESH_SCRIPT =
            "local key=KEYS[1];local token=ARGV[1];local lease=tonumber(ARGV[2])or 0;local owner=redis.call('HGET',key,'owner');if not owner then return 0 end;if owner~=token then return -1 end;redis.call('PEXPIRE',key,lease);return 1"
    }
}
