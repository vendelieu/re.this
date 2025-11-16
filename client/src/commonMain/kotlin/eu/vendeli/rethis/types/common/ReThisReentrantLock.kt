package eu.vendeli.rethis.types.common

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.generic.pTtl
import eu.vendeli.rethis.command.scripting.eval
import eu.vendeli.rethis.shared.types.LockLostException
import eu.vendeli.rethis.shared.utils.unwrap
import eu.vendeli.rethis.types.interfaces.ReDistributedLock
import kotlinx.coroutines.*
import kotlin.concurrent.atomics.*
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.TimeSource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
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
 *
 * Watchdog
 * - Refreshes TTL using REFRESH_SCRIPT (atomic: check owner then pexpire)
 * - Runs on a separate SupervisorScope to avoid interfering with caller coroutine
 */
@OptIn(ExperimentalAtomicApi::class)
internal class ReThisReentrantLock(
    private val client: ReThis,
    private val key: String,
    private val referenceJob: Job,
    private val defaultLeaseMs: Long = 30_000L,
    private val backoffBaseMs: Long = 50L,
    private val backoffCapMs: Long = 1000L,
) : ReDistributedLock {

    @OptIn(ExperimentalUuidApi::class)
    private val instanceId = "inst:${Uuid.random()}"

    // stable owner token derived from referenceJob in-process; null when not currently owning
    private val ownerIdLocal = AtomicReference<String?>(deriveOwnerId(referenceJob))

    // local fast-path counter (keeps quick reentrant checks) — authoritative counter lives in Redis
    private val localDepth = AtomicInt(0)

    // Watchdog scope + job
    private val watchdogScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val watchdogJob = AtomicReference<Job?>(null)

    // ------------------------
    // Public API
    // ------------------------

    override suspend fun tryLock(waitTime: Duration, leaseTime: Duration): Boolean {
        currentCoroutineContext().ensureActive()
        val job = currentCoroutineContext()[Job] ?: error("No Job in context")

        val waitMs = waitTime.toLong(DurationUnit.MILLISECONDS).coerceAtLeast(0)
        val leaseMs = leaseTime.toLong(DurationUnit.MILLISECONDS).let { if (it > 0) it else defaultLeaseMs }
        val start = TimeSource.Monotonic.markNow()

        // Fast-path when we already believe we are owner locally
        if (isSameOwner(job) && localDepth.load() > 0) {
            val token = ownerIdLocal.load() ?: ""
            val r = acquireScript(token, leaseMs)
            when (r) {
                1 -> {
                    localDepth.incrementAndFetch()
                    // ensure TTL is healthy
                    verifyTTL(leaseMs)
                    return true
                }

                0 -> { /* someone else owns it now — fall through to acquisition loop */
                }

                -1 -> throw LockLostException("Token mismatch during optimistic reenter for key=$key")
                -2 -> throw LockLostException("Corrupted lock state during optimistic reenter for key=$key")
            }
        }

        var lastBackoff = backoffBaseMs
        val token = makeTokenForJob(job)

        while (true) {
            currentCoroutineContext().ensureActive()

            val r = acquireScript(token, leaseMs)
            when (r) {
                1 -> {
                    ownerIdLocal.store(token)
                    localDepth.incrementAndFetch()
                    // start watchdog only on the initial ownership (depth==1)
                    startWatchdog(token, leaseMs)
                    verifyTTL(leaseMs)
                    return true
                }

                0 -> { /* someone else holds it; retry or return depending on wait */
                }

                -1 -> {
                    throw LockLostException("Lock currently held by another owner for key=$key")
                }

                -2 -> {
                    throw LockLostException("Corrupted lock state for key=$key")
                }
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

        val token = ownerIdLocal.load() ?: return false

        // decrement local depth
        val newLocal = localDepth.updateAndFetch { cur -> if (cur > 0) cur - 1 else 0 }

        val isFinal = newLocal == 0
        val r = releaseScript(token, if (isFinal) defaultLeaseMs else defaultLeaseMs)

        when (r) {
            1 -> {
                if (isFinal) {
                    stopWatchdog()
                    ownerIdLocal.store(null)
                    localDepth.store(0)
                }
                return true
            }

            0 -> throw LockLostException("Lock already missing/expired for key=$key during unlock")
            -1 -> throw LockLostException("Unlock attempted by non-owner for key=$key")
            -2 -> throw LockLostException("Corrupted lock state for key=$key during unlock")
            else -> throw IllegalStateException("Unexpected unlock script response: $r for key=$key")
        }
    }

    // ------------------------
    // Watchdog
    // ------------------------

    private fun startWatchdog(token: String, leaseMs: Long) {
        stopWatchdog()
        watchdogJob.compareAndSet(
            null,
            watchdogScope.launch {
                val base = (leaseMs / 3).coerceAtLeast(50L)
                while (isActive) {
                    // add small jitter
                    val jitter = Random.nextLong(0, base / 5 + 1)
                    delay(base + jitter)
                    try {
                        val r = refreshScript(token, leaseMs)
                        if (r != 1) {
                            // any non-success indicates the lock is lost or corrupted
                            throw LockLostException("Watchdog failed to refresh key=$key response=$r")
                        }
                    } catch (t: Throwable) {
                        // escalate — cancel watchdog and surface exception asynchronously
                        stopWatchdog()
                        // We cannot throw from here to caller; instead, best-effort: log and cancel owner
                        // For library: consider invoking a user-provided callback here
                        // Re-throw using coroutine cancellation to stop watchdog
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

    private fun isSameOwner(job: Job): Boolean = job.isRelativeTo(referenceJob) && ownerIdLocal.load() != null

    @OptIn(ExperimentalUuidApi::class)
    private fun makeTokenForJob(job: Job): String {
        val ownerPart = if (job.isRelativeTo(referenceJob)) deriveOwnerId(referenceJob) else deriveOwnerId(job)
        return "$instanceId:$ownerPart:${Uuid.random()}"
    }

    private fun deriveOwnerId(job: Job): String = "job_${job.hashCode()}_${Random.nextInt(0xFFFF)}"

    private fun decorrelatedExponentialJitter(prev: Long): Long {
        val previous = prev * 3
        val newDelay = Random.nextLong(backoffBaseMs, previous)
        return newDelay.coerceAtMost(backoffCapMs)
    }

    private suspend fun verifyTTL(leaseMs: Long) = runCatching {
        val ttl = client.pTtl(key)
        if (ttl < leaseMs / 4) {
            throw LockLostException("Redis TTL too low for key=$key ($ttl ms) after acquire; lease=$leaseMs")
        }
    }.onFailure { t ->
        throw LockLostException("PTTL verification failed for key=$key: ${t.message}")
    }

    // ------------------------
    // Redis script wrappers
    // ------------------------

    private suspend fun acquireScript(token: String, leaseMs: Long): Int =
        runCatching {
            val r = client.eval(ACQUIRE_SCRIPT, key = arrayOf(key), arg = listOf(token, leaseMs.toString()))
            return r.unwrap<Long?>()?.toInt() ?: -2
        }.getOrElse { -2 }

    private suspend fun releaseScript(token: String, leaseMs: Long): Int =
        runCatching {
            val r = client.eval(RELEASE_SCRIPT, key = arrayOf(key), arg = listOf(token, leaseMs.toString()))
            return r.unwrap<Long?>()?.toInt() ?: -2
        }.getOrElse { -2 }


    private suspend fun refreshScript(token: String, leaseMs: Long): Int =
        runCatching {
            val r = client.eval(REFRESH_SCRIPT, key = arrayOf(key), arg = listOf(token, leaseMs.toString()))
            return r.unwrap<Long?>()?.toInt() ?: -2
        }.getOrElse { -2 }

    companion object {
        /**
         *             -- ACQUIRE_SCRIPT
         *             -- ARGV[1] = token
         *             -- ARGV[2] = lease (ms)
         *             local key = KEYS[1]
         *             local token = ARGV[1]
         *             local lease = tonumber(ARGV[2]) or 0
         *
         *             -- fetch fields safely
         *             local owner = redis.call('HGET', key, 'owner')
         *             local count = redis.call('HGET', key, 'count')
         *             local ver = redis.call('HGET', key, 'ver')
         *
         *             if count and type(count) == 'string' then count = tonumber(count) end
         *             if ver and type(ver) == 'string' then ver = tonumber(ver) end
         *
         *             -- absent lock => initialize
         *             if not owner then
         *               redis.call('HSET', key, 'owner', token)
         *               redis.call('HSET', key, 'count', 1)
         *               redis.call('HSET', key, 'ver', 1)
         *               redis.call('PEXPIRE', key, lease)
         *               return 1
         *             end
         *
         *             -- corrupted: count not number or ver mismatch
         *             if (not count) or (ver ~= 1) then
         *               return -2
         *             end
         *
         *             -- reentrant by same owner
         *             if owner == token then
         *               local newc = count + 1
         *               redis.call('HSET', key, 'count', newc)
         *               redis.call('PEXPIRE', key, lease)
         *               return 1
         *             end
         *
         *             -- owned by someone else
         *             return 0
         */
        private const val ACQUIRE_SCRIPT =
            "local key=KEYS[1];local token=ARGV[1];local lease=tonumber(ARGV[2])or 0;local owner=redis.call('HGET',key,'owner');local count=redis.call('HGET',key,'count');local ver=redis.call('HGET',key,'ver');if count and type(count)=='string'then count=tonumber(count)end;if ver and type(ver)=='string'then ver=tonumber(ver)end;if not owner then redis.call('HSET',key,'owner',token);redis.call('HSET',key,'count',1);redis.call('HSET',key,'ver',1);redis.call('PEXPIRE',key,lease);return 1 end;if(not count)or(ver~=1)then return -2 end;if owner==token then local newc=count+1;redis.call('HSET',key,'count',newc);redis.call('PEXPIRE',key,lease);return 1 end;return 0"

        /**
         *             -- RELEASE_SCRIPT
         *             -- ARGV[1] = token
         *             -- ARGV[2] = lease (ms) — used only if decrementing count
         *             local key = KEYS[1]
         *             local token = ARGV[1]
         *             local lease = tonumber(ARGV[2]) or 0
         *
         *             local owner = redis.call('HGET', key, 'owner')
         *             if not owner then return 0 end
         *             local count = redis.call('HGET', key, 'count')
         *             local ver = redis.call('HGET', key, 'ver')
         *
         *             if type(count) == 'string' then count = tonumber(count) end
         *             if type(ver) == 'string' then ver = tonumber(ver) end
         *
         *             if not count or ver ~= 1 then return -2 end
         *             if owner ~= token then return -1 end
         *
         *             local newc = count - 1
         *             if newc > 0 then
         *               redis.call('HSET', key, 'count', newc)
         *               redis.call('PEXPIRE', key, lease)
         *               return 1
         *             else
         *               redis.call('DEL', key)
         *               return 1
         *             end
         */
        private const val RELEASE_SCRIPT =
            "local key=KEYS[1];local token=ARGV[1];local lease=tonumber(ARGV[2])or 0;local owner=redis.call('HGET',key,'owner');if not owner then return 0 end;local count=redis.call('HGET',key,'count');local ver=redis.call('HGET',key,'ver');if type(count)=='string' then count=tonumber(count) end;if type(ver)=='string' then ver=tonumber(ver) end;if not count or ver~=1 then return -2 end;if owner~=token then return -1 end;local newc=count-1;if newc>0 then redis.call('HSET',key,'count',newc);redis.call('PEXPIRE',key,lease);return 1 else redis.call('DEL',key);return 1 end"

        /**
         *             -- REFRESH_SCRIPT
         *             -- ARGV[1] = token
         *             -- ARGV[2] = lease (ms)
         *             local key = KEYS[1]
         *             local token = ARGV[1]
         *             local lease = tonumber(ARGV[2]) or 0
         *
         *             local owner = redis.call('HGET', key, 'owner')
         *             if not owner then return 0 end
         *             if owner ~= token then return -1 end
         *             -- refresh TTL
         *             redis.call('PEXPIRE', key, lease)
         *             return 1
         */
        private const val REFRESH_SCRIPT =
            "local key=KEYS[1];local token=ARGV[1];local lease=tonumber(ARGV[2])or 0;local owner=redis.call('HGET',key,'owner');if not owner then return 0 end;if owner~=token then return -1 end;redis.call('PEXPIRE',key,lease);return 1"
    }
}

/**
 * Checks whether [this] job is the same as [referenceJob] or an ancestor of it.
 */
@OptIn(ExperimentalCoroutinesApi::class)
private fun Job.isRelativeTo(referenceJob: Job): Boolean {
    if (this === referenceJob) return true
    var current: Job? = referenceJob
    while (current != null) {
        if (current === this) return true
        current = current.parent
    }
    return false
}
