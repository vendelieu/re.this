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
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Tests for standard ReDistributedLock (per-coroutine ownership).
 */
class RedisDistributedLockTest : ReThisTestCtx() {
    // ==================== REENTRANCY TESTS ====================

    @Test
    suspend fun `reentrancy in same coroutine increments and requires matching unlocks`() {
        val lockName = "rethis:lock:reentrant:${UUID.randomUUID()}"
        val lock = client.reDistributedLock(lockName)
        val otherClient = createClient()
        val otherOwner = otherClient.reDistributedLock(lockName)

        // Acquire twice (reentrant)
        lock.lock(2.seconds)
        lock.lock(2.seconds)

        // First unlock should still keep the lock held by the same owner
        lock.unlock().shouldBeTrue()
        // Another owner should not be able to acquire yet
        otherOwner.tryLock(waitTime = 100.milliseconds, leaseTime = 1.seconds).shouldBeFalse()
        // Second unlock should fully release the lock
        lock.unlock().shouldBeTrue()

        // Can be acquired again by other owner
        otherOwner.tryLock(waitTime = 300.milliseconds, leaseTime = 1.seconds).shouldBeTrue()
        otherOwner.unlock().shouldBeTrue()
    }

    @Test
    suspend fun `deep reentrancy works correctly`() {
        val lockName = "rethis:lock:deep-reentrant:${UUID.randomUUID()}"
        val lock = client.reDistributedLock(lockName)

        // Acquire 5 times
        repeat(5) { lock.lock(2.seconds) }

        // Unlock 4 times - lock should still be held
        repeat(4) { lock.unlock().shouldBeTrue() }

        // Other owner still can't acquire
        val otherClient = createClient()
        val otherOwner = otherClient.reDistributedLock(lockName)
        otherOwner.tryLock(waitTime = 50.milliseconds, leaseTime = 1.seconds).shouldBeFalse()

        // Final unlock releases
        lock.unlock().shouldBeTrue()

        // Now other owner can acquire
        otherOwner.tryLock(waitTime = 100.milliseconds, leaseTime = 1.seconds).shouldBeTrue()
        otherOwner.unlock().shouldBeTrue()
    }

    // ==================== MUTUAL EXCLUSION TESTS ====================

    @Test
    suspend fun `same instance blocks different coroutines`() = coroutineScope {
        val lockName = "rethis:lock:same-instance-blocks:${UUID.randomUUID()}"
        val shared = client.reDistributedLock(lockName)

        val inCritical = AtomicBoolean(false)
        val visits = AtomicInteger(0)

        suspend fun critical() {
            shared.lock(1.seconds)
            try {
                val first = inCritical.compareAndSet(false, true)
                if (!first) error("Overlap with same instance!")
                delay(30)
                visits.incrementAndGet()
            } finally {
                inCritical.set(false)
                shared.unlock().shouldBeTrue()
            }
        }

        val j1 = launch { repeat(3) { critical() } }
        val j2 = launch { repeat(3) { critical() } }
        listOf(j1, j2).joinAll()

        visits.get().shouldBeExactly(6)
    }

    @Test
    suspend fun `mutual exclusion across different clients prevents overlap`() = coroutineScope {
        val lockName = "rethis:lock:mutex:${UUID.randomUUID()}"
        val lock1 = client.reDistributedLock(lockName)
        val otherClient: ReThis = createClient()
        val lock2 = otherClient.reDistributedLock(lockName)

        val inCritical = AtomicBoolean(false)
        val visits = AtomicInteger(0)

        suspend fun critical(lock: ReDistributedLock) {
            lock.lock(2.seconds)
            try {
                val first = inCritical.compareAndSet(false, true)
                if (!first) error("Overlapped critical section detected")
                delay(40)
                visits.incrementAndGet()
            } finally {
                inCritical.set(false)
                lock.unlock().shouldBeTrue()
            }
        }

        val j1 = launch { repeat(3) { critical(lock1) } }
        val j2 = launch { repeat(3) { critical(lock2) } }
        listOf(j1, j2).joinAll()

        visits.get().shouldBeExactly(6)
    }

    @Test
    suspend fun `separate lock instances in same coroutine scope block each other`() = coroutineScope {
        val lockName = "rethis:lock:separate-instances:${UUID.randomUUID()}"

        val inCritical = AtomicBoolean(false)
        val visits = AtomicInteger(0)

        val j1 = launch {
            val lock = client.reDistributedLock(lockName)
            repeat(3) {
                lock.lock(1.seconds)
                try {
                    val first = inCritical.compareAndSet(false, true)
                    if (!first) error("Overlap detected!")
                    delay(30)
                    visits.incrementAndGet()
                } finally {
                    inCritical.set(false)
                    lock.unlock().shouldBeTrue()
                }
            }
        }
        val j2 = launch {
            val lock = client.reDistributedLock(lockName)
            repeat(3) {
                lock.lock(1.seconds)
                try {
                    val first = inCritical.compareAndSet(false, true)
                    if (!first) error("Overlap detected!")
                    delay(30)
                    visits.incrementAndGet()
                } finally {
                    inCritical.set(false)
                    lock.unlock().shouldBeTrue()
                }
            }
        }
        listOf(j1, j2).joinAll()
        visits.get().shouldBeExactly(6)
    }

    // ==================== TRYLOCK TESTS ====================

    @Test
    suspend fun `tryLock returns true when lock is free`() {
        val lockName = "rethis:lock:trylock-free:${UUID.randomUUID()}"
        val lock = client.reDistributedLock(lockName)

        lock.tryLock(waitTime = 0.milliseconds, leaseTime = 1.seconds).shouldBeTrue()
        lock.unlock().shouldBeTrue()
    }

    @Test
    suspend fun `tryLock times out when already held by another owner`() = coroutineScope {
        val lockName = "rethis:lock:timeout:${UUID.randomUUID()}"
        val owner1 = client.reDistributedLock(lockName)
        val otherClient = createClient()
        val owner2 = otherClient.reDistributedLock(lockName)

        owner1.lock(5.seconds)
        try {
            val acquired = owner2.tryLock(waitTime = 100.milliseconds, leaseTime = 1.seconds)
            acquired.shouldBeFalse()
        } finally {
            owner1.unlock().shouldBeTrue()
        }
    }

    @Test
    suspend fun `tryLock with zero wait fails immediately when locked`() = coroutineScope {
        val lockName = "rethis:lock:zero-wait:${UUID.randomUUID()}"
        val owner1 = client.reDistributedLock(lockName)
        val otherClient = createClient()
        val owner2 = otherClient.reDistributedLock(lockName)

        owner1.lock(5.seconds)
        try {
            owner2.tryLock(waitTime = 0.milliseconds, leaseTime = 1.seconds).shouldBeFalse()
        } finally {
            owner1.unlock().shouldBeTrue()
        }
    }

    @Test
    suspend fun `different client cannot unlock another client's lock`() = coroutineScope {
        val lockName = "rethis:lock:non-owner-unlock:${UUID.randomUUID()}"
        val lock1 = client.reDistributedLock(lockName)
        val otherClient = createClient()
        val lock2 = otherClient.reDistributedLock(lockName)

        lock1.lock(2.seconds)

        // Different lock instance (different client/token) should fail
        shouldThrow<LockLostException> {
            lock2.unlock()
        }

        // Original lock can still unlock
        lock1.unlock().shouldBeTrue()
    }

    // ==================== UNLOCK TESTS ====================

    @Test
    suspend fun `unlock frees the lock for other owners`() = coroutineScope {
        val lockName = "rethis:lock:unlock-frees:${UUID.randomUUID()}"
        val owner1 = client.reDistributedLock(lockName)
        val otherClient = createClient()
        val owner2 = otherClient.reDistributedLock(lockName)

        owner1.lock(2.seconds) // Use longer lease to avoid expiry during test
        // While held, other owner should not acquire
        owner2.tryLock(waitTime = 100.milliseconds, leaseTime = 1.seconds).shouldBeFalse()
        // After release it should acquire quickly
        owner1.unlock().shouldBeTrue()
        owner2.tryLock(waitTime = 300.milliseconds, leaseTime = 1.seconds).shouldBeTrue()
        owner2.unlock().shouldBeTrue()
    }

    // Removed: `non-owner coroutine cannot unlock` test
    // Per-coroutine ownership checking on unlock is incompatible with withLock's
    // withContext(NonCancellable) pattern. Protection against unauthorized unlock
    // comes from different lock instances having different Redis tokens.

    @Test
    suspend fun `different client cannot unlock`() = coroutineScope {
        val lockName = "rethis:lock:ownership:${UUID.randomUUID()}"
        val owner1 = client.reDistributedLock(lockName)
        val otherClient = createClient()
        val owner2 = otherClient.reDistributedLock(lockName)

        owner1.lock(2.seconds)
        try {
            // Another owner should fail to unlock
            shouldThrow<LockLostException> {
                owner2.unlock()
            }

            // Original owner can still unlock
            owner1.unlock().shouldBeTrue()
        } finally {
            runCatching { owner1.unlock() }
        }
    }

    // ==================== WITHLOCK TESTS ====================

    @Test
    suspend fun `withLock executes block and releases`() {
        val lockName = "rethis:lock:withlock:${UUID.randomUUID()}"
        val lock = client.reDistributedLock(lockName)
        val otherClient = createClient()
        val otherLock = otherClient.reDistributedLock(lockName)

        val result = lock.withLock(1.seconds) {
            // Lock should be held
            otherLock.tryLock(waitTime = 50.milliseconds, leaseTime = 1.seconds).shouldBeFalse()
            "success"
        }

        result.shouldBe("success")

        // Lock should be released
        otherLock.tryLock(waitTime = 100.milliseconds, leaseTime = 1.seconds).shouldBeTrue()
        otherLock.unlock().shouldBeTrue()
    }

    @Test
    suspend fun `withLock releases on exception`() {
        val lockName = "rethis:lock:withlock-exception:${UUID.randomUUID()}"
        val lock = client.reDistributedLock(lockName)
        val otherClient = createClient()
        val otherLock = otherClient.reDistributedLock(lockName)

        runCatching {
            lock.withLock(1.seconds) {
                error("Test exception")
            }
        }

        // Lock should be released despite exception
        otherLock.tryLock(waitTime = 100.milliseconds, leaseTime = 1.seconds).shouldBeTrue()
        otherLock.unlock().shouldBeTrue()
    }

    @Test
    suspend fun `withLock releases on cancellation`() = coroutineScope {
        val lockName = "rethis:lock:cancel:${UUID.randomUUID()}"
        val lock = client.reDistributedLock(lockName)

        val entered = CompletableDeferred<Unit>()
        val willSuspend = CompletableDeferred<Unit>()

        val job = launch {
            lock.withLock(2.seconds) {
                entered.complete(Unit)
                willSuspend.await()
            }
        }

        entered.await()

        job.cancel()
        willSuspend.complete(Unit)
        job.join()

        // Lock should be released
        lock.tryLock(waitTime = 500.milliseconds, leaseTime = 1.seconds).shouldBeTrue()
        lock.unlock().shouldBeTrue()
    }

    // ==================== EDGE CASES ====================

    @Test
    suspend fun `lock works after previous owner releases`() = coroutineScope {
        val lockName = "rethis:lock:sequential:${UUID.randomUUID()}"

        val completed = AtomicInteger(0)

        val j1 = launch {
            val lock = client.reDistributedLock(lockName)
            lock.lock(1.seconds)
            delay(50)
            completed.incrementAndGet()
            lock.unlock()
        }

        delay(10) // Let j1 acquire first

        val j2 = launch {
            val otherClient = createClient()
            val lock = otherClient.reDistributedLock(lockName)
            lock.lock(1.seconds) // Should wait for j1 to release
            completed.incrementAndGet()
            lock.unlock()
        }

        listOf(j1, j2).joinAll()
        completed.get().shouldBeExactly(2)
    }
}
