package eu.vendeli.rethis.utils

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.scripting.eval
import eu.vendeli.rethis.command.server.info
import eu.vendeli.rethis.shared.utils.unwrap
import eu.vendeli.rethis.types.interfaces.ReDistributedLock
import eu.vendeli.rethis.shared.types.LockLostException
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.test.config.DefaultTestConfig
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Lease watchdog behaviour: a failed refresh must not cancel the caller and the renewal interval must stay positive.
 */
class RedisLockWatchdogTest : ReThisTestCtx() {
    init {
        defaultTestConfig = DefaultTestConfig(timeout = 60.seconds)
    }

    private suspend inline fun <reified T> ReThis.lua(script: String, key: String, vararg args: String): T? =
        eval(script = script, key = arrayOf(key), arg = args.toList()).unwrap<T?>()

    private suspend fun ReThis.evalCalls(): Int = Regex("cmdstat_eval:calls=(\\d+)")
        .find(info("commandstats"))
        ?.groupValues
        ?.get(1)
        ?.toInt() ?: 0

    private suspend fun ReThis.owner(key: String) = lua<String>("return redis.call('HGET', KEYS[1], 'owner')", key)!!

    private suspend fun ReThis.ttl(key: String) = lua<Long>("return redis.call('PTTL', KEYS[1])", key)!!

    /** Makes the refresh script fail (WRONGTYPE) until [restore] puts the lock hash back. */
    private suspend fun ReThis.corrupt(key: String) =
        lua<Long>("redis.call('DEL', KEYS[1]); redis.call('SET', KEYS[1], 'x'); return 1", key)

    private suspend fun ReThis.restore(key: String, owner: String, leaseMs: Long) = lua<Long>(
        "redis.call('DEL', KEYS[1]); redis.call('HSET', KEYS[1], 'owner', ARGV[1], 'count', 1); " +
            "redis.call('PEXPIRE', KEYS[1], ARGV[2]); return 1",
        key,
        owner,
        leaseMs.toString(),
    )

    private suspend fun assertSurvivesFailedRefresh(name: String, lock: ReDistributedLock) {
        val leaseMs = 600L
        lock.tryLock(waitTime = 0.milliseconds, leaseTime = leaseMs.milliseconds) shouldBe true
        val callerJob = currentCoroutineContext()[Job]!!
        val owner = client.owner(name)

        client.corrupt(name)
        delay(500.milliseconds) // at least two refresh ticks fail
        callerJob.isActive shouldBe true

        client.restore(name, owner, leaseMs = 250)
        // a later refresh succeeded when the lease is extended back towards the full lease
        eventually(5.seconds) { client.ttl(name) shouldBeGreaterThan 400L }
        callerJob.isActive shouldBe true
        lock.unlock() shouldBe true
    }

    @Test
    suspend fun `failed refresh keeps the caller alive and a later refresh still succeeds`() = withTimeout(30.seconds) {
        val name = "rethis:lock:watchdog:${UUID.randomUUID()}"
        assertSurvivesFailedRefresh(name, client.reDistributedLock(name))
    }

    @Test
    suspend fun `hierarchical failed refresh keeps the reference job active`() = withTimeout(30.seconds) {
        val name = "rethis:lock:watchdog-hier:${UUID.randomUUID()}"
        coroutineScope {
            assertSurvivesFailedRefresh(name, client.reHierarchicalDistributedLock(name))
        }
    }

    @Test
    suspend fun `tiny lease does not make the watchdog spin`() = withTimeout(30.seconds) {
        val name = "rethis:lock:watchdog-tiny:${UUID.randomUUID()}"
        val lock = client.reDistributedLock(name)
        val before = client.evalCalls()

        lock.tryLock(waitTime = 0.milliseconds, leaseTime = 2.milliseconds) shouldBe true
        delay(200.milliseconds)
        val calls = client.evalCalls() - before
        try {
            lock.unlock()
        } catch (e: LockLostException) {
            // the 2 ms lease may legitimately have expired between ticks
        }

        // one acquire plus at most one refresh per millisecond
        calls shouldBeLessThanOrEqual 300
    }
}
