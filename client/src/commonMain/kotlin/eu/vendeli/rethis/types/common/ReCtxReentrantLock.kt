package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.scripting.eval
import eu.vendeli.rethis.shared.types.LockLostException
import eu.vendeli.rethis.shared.utils.unwrap
import eu.vendeli.rethis.types.interfaces.ReDistributedLock
import kotlinx.coroutines.*
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
 * Hierarchical distributed reentrant lock with Job-tree-based ownership.
 */

private data class HierarchicalLockState(
    val token: String,
    val depth: Int,
)

@OptIn(ExperimentalAtomicApi::class)
internal class ReCtxReentrantLock(
    private val client: ReThis,
    private val key: String,
    private val referenceJob: Job,
    private val defaultLeaseMs: Long = 30_000L,
    private val backoffBaseMs: Long = 50L,
    private val backoffCapMs: Long = 1000L,
) : ReDistributedLock {

    @OptIn(ExperimentalUuidApi::class)
    private val instanceId = "inst:${Uuid.random()}"

    @OptIn(ExperimentalUuidApi::class)
    private val stableToken: String = "$instanceId:job_${referenceJob.hashCode()}:${Uuid.random()}"

    private val state = AtomicReference<HierarchicalLockState?>(null)
    private val watchdogJob = AtomicReference<Job?>(null)

    override suspend fun tryLock(waitTime: Duration, leaseTime: Duration): Boolean {
        currentCoroutineContext().ensureActive()
        val job = currentCoroutineContext()[Job] ?: error("No Job in context")

        val waitMs = waitTime.toLong(DurationUnit.MILLISECONDS).coerceAtLeast(0)
        val leaseMs = leaseTime.toLong(DurationUnit.MILLISECONDS).let { if (it > 0) it else defaultLeaseMs }
        val start = TimeSource.Monotonic.markNow()

        if (!job.isRelativeTo(referenceJob)) {
            throw IllegalStateException("Lock accessed from coroutine outside its hierarchy for key=$key")
        }

        val currentState = state.load()
        if (currentState != null) {
            val r = acquireScript(stableToken, leaseMs)
            when (r) {
                1 -> {
                    state.store(currentState.copy(depth = currentState.depth + 1))
                    return true
                }
                0 -> {
                    stopWatchdog()
                    state.store(null)
                }
                -1 -> throw LockLostException("Token mismatch during optimistic reenter for key=$key")
                -2 -> throw LockLostException("Corrupted lock state during optimistic reenter for key=$key")
            }
        }

        var lastBackoff = backoffBaseMs

        while (true) {
            currentCoroutineContext().ensureActive()

            when (acquireScript(stableToken, leaseMs)) {
                1 -> {
                    state.store(HierarchicalLockState(token = stableToken, depth = 1))
                    startWatchdog(stableToken, leaseMs)
                    return true
                }
                0 -> { /* someone else holds it */ }
                -1 -> throw LockLostException("Lock currently held by another owner for key=$key")
                -2 -> throw LockLostException("Corrupted lock state for key=$key")
            }

            if (waitMs == 0L || start.elapsedNow().inWholeMilliseconds >= waitMs) return false

            val delayMs = decorrelatedExponentialJitter(lastBackoff).coerceAtMost(backoffCapMs)
            lastBackoff = delayMs
            delay(delayMs)
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
        currentCoroutineContext()[Job] ?: error("No Job in context")

        val currentState = state.load()
            ?: throw LockLostException("Unlock called but lock not held for key=$key")

        val newDepth = currentState.depth - 1
        val isFinal = newDepth == 0

        val r = releaseScript(currentState.token, defaultLeaseMs)

        when (r) {
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

    @OptIn(DelicateCoroutinesApi::class)
    private fun startWatchdog(token: String, leaseMs: Long) {
        stopWatchdog()
        // Use GlobalScope so watchdog doesn't block parent coroutine completion
        val job = GlobalScope.launch(Dispatchers.Default) {
            val base = (leaseMs / 3).coerceAtLeast(50L)
            while (isActive) {
                val jitter = Random.nextLong(0, base / 5 + 1)
                delay(base + jitter)
                try {
                    val r = refreshScript(token, leaseMs)
                    if (r != 1) break
                } catch (_: Throwable) {
                    break
                }
            }
        }
        watchdogJob.store(job)
    }

    private fun stopWatchdog() {
        watchdogJob.exchange(null)?.cancel()
    }

    private fun decorrelatedExponentialJitter(prev: Long): Long {
        val previous = prev * 3
        val newDelay = Random.nextLong(backoffBaseMs, previous.coerceAtLeast(backoffBaseMs + 1))
        return newDelay.coerceAtMost(backoffCapMs)
    }

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
