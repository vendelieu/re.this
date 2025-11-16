package eu.vendeli.rethis.utils

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.shared.types.LockLostException
import eu.vendeli.rethis.types.interfaces.ReDistributedLock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class RedisDistributedLockTest : ReThisTestCtx() {

    @Test
    suspend fun `reentrancy in same coroutine increments and requires matching unlocks`() {
        val lockName = "rethis:lock:reentrant:${UUID.randomUUID()}"
        val lock = client.reDistributedLock(lockName)
        val otherOwner = createClient().reDistributedLock(lockName)

        // Acquire twice (reentrant)
        lock.lock(2.seconds)
        lock.lock(2.seconds)

        // First unlock should still keep the lock held by the same owner
        lock.unlock().shouldBeTrue()
        // Another owner should not be able to acquire yet
        otherOwner.tryLock(waitTime = 100.milliseconds, leaseTime = 1.seconds).shouldBeFalse()
        // Second unlock should fully release the lock
        lock.unlock().shouldBeTrue()

        // Can be acquired again
        otherOwner.tryLock(waitTime = 300.milliseconds, leaseTime = 1.seconds) shouldBe true
        otherOwner.unlock().shouldBeTrue()
    }

    @Test
    suspend fun `mutual exclusion across coroutines prevents overlap`() = coroutineScope {
        val lockName = "rethis:lock:mutex:${UUID.randomUUID()}"
        val lock1 = client.reDistributedLock(lockName)
        val otherClient: ReThis = createClient()
        val lock2 = otherClient.reDistributedLock(lockName)

        val inCritical = AtomicBoolean(false)
        var visits = 0

        suspend fun critical(lock: ReDistributedLock) {
            lock.lock(2.seconds)
            try {
                // detect overlap
                val first = inCritical.compareAndSet(false, true)
                if (!first) error("Overlapped critical section detected")
                val before = visits
                // simulate work
                delay(50)
                visits = before + 1
            } finally {
                inCritical.set(false)
                lock.unlock().shouldBeTrue()
            }
        }

        val j1 = launch { repeat(5) { critical(lock1) } }
        val j2 = launch { repeat(5) { critical(lock2) } }
        listOf(j1, j2).joinAll()

        visits.shouldBeExactly(10)
    }

    @Test
    suspend fun `tryLock times out when already held by another owner`() = coroutineScope {
        val lockName = "rethis:lock:timeout:${UUID.randomUUID()}"
        val owner1 = client.reDistributedLock(lockName)
        val owner2 = createClient().reDistributedLock(lockName)

        owner1.lock(5.seconds)
        try {
            val acquired = owner2.tryLock(waitTime = 100.milliseconds, leaseTime = 1.seconds)
            acquired.shouldBeFalse()
        } finally {
            owner1.unlock().shouldBeTrue()
        }
    }

    @Test
    suspend fun `unlock frees the lock for other owners`() = coroutineScope {
        val lockName = "rethis:lock:ttl:${UUID.randomUUID()}"
        val owner1 = client.reDistributedLock(lockName)
        val owner2 = createClient().reDistributedLock(lockName)

        owner1.lock(300.milliseconds)
        // While held, other owner should not acquire
        owner2.tryLock(waitTime = 200.milliseconds, leaseTime = 1.seconds).shouldBeFalse()
        // After release it should acquire quickly
        owner1.unlock().shouldBeTrue()
        val ok = owner2.tryLock(waitTime = 300.milliseconds, leaseTime = 1.seconds)
        ok.shouldBeTrue()
        owner2.unlock().shouldBeTrue()
    }

    @Test
    suspend fun `non-owner cannot unlock and does not delete the key`() = coroutineScope {
        val lockName = "rethis:lock:ownership:${UUID.randomUUID()}"
        val owner1 = client.reDistributedLock(lockName)
        val owner2 = createClient().reDistributedLock(lockName)

        owner1.lock(2.seconds)
        try {
            // Another owner should fail to unlock
            shouldThrow<LockLostException> {
                owner2.unlock().shouldBeFalse()
            }

            // Original owner can still unlock
            owner1.unlock().shouldBeTrue()
        } finally {
            // best-effort release if still held
            runCatching { owner1.unlock() }
        }
    }

    @Test
    suspend fun `same instance used from two coroutines does not overlap`() = coroutineScope {
        val lockName = "rethis:lock:same-instance:${UUID.randomUUID()}"
        val shared = client.reDistributedLock(lockName)

        val inCritical = AtomicBoolean(false)
        var visits = 0

        suspend fun critical() {
            shared.lock(1.seconds)
            try {
                val first = inCritical.compareAndSet(false, true)
                if (!first) error("Overlap with same instance!")
                delay(40)
                visits += 1
            } finally {
                inCritical.set(false)
                shared.unlock().shouldBeTrue()
            }
        }

        val j1 = launch { repeat(5) { critical() } }
        val j2 = launch { repeat(5) { critical() } }
        listOf(j1, j2).joinAll()
        visits.shouldBeExactly(10)
    }

    @Test
    suspend fun `withLock releases on cancellation`() = coroutineScope {
        val lockName = "rethis:lock:cancel:${UUID.randomUUID()}"
        val lock = client.reDistributedLock(lockName)

        val job = launch {
            lock.withLock(2.seconds) {
                // Hold for some time, then get cancelled
                delay(5000)
            }
        }
        // Give it time to acquire
        delay(100)
        // Cancel the job; withLock should unlock in finally
        job.cancel()
        job.join()

        // Now we should be able to acquire quickly
        lock.tryLock(waitTime = 300.milliseconds, leaseTime = 1.seconds).shouldBeTrue()
        lock.unlock().shouldBeTrue()
    }
}
