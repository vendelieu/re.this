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
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class RedisHierarchicalDistributedLockTest : ReThisTestCtx() {
    @Test
    suspend fun `reentrancy in same coroutine increments and requires matching unlocks`() {
        val lockName = "rethis:lock:reentrant:${UUID.randomUUID()}"
        val lock = client.reHierarchicalDistributedLock(lockName)
        val otherOwner = createClient().reHierarchicalDistributedLock(lockName)

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
        val otherClient: ReThis = createClient()

        val inCritical = AtomicBoolean(false)
        val visits = java.util.concurrent.atomic.AtomicInteger(0)  // Use AtomicInteger

        suspend fun critical(lock: ReDistributedLock) {
            lock.lock(2.seconds)
            try {
                // detect overlap
                val first = inCritical.compareAndSet(false, true)
                if (!first) error("Overlapped critical section detected")
                // simulate work
                delay(50)
                visits.incrementAndGet()  // Atomic increment
            } finally {
                inCritical.set(false)
                lock.unlock().shouldBeTrue()
            }
        }

        // Create locks INSIDE the launched coroutines so each has its own referenceJob
        val j1 = launch {
            val lock1 = client.reHierarchicalDistributedLock(lockName)
            repeat(5) { critical(lock1) }
        }
        val j2 = launch {
            val lock2 = otherClient.reHierarchicalDistributedLock(lockName)
            repeat(5) { critical(lock2) }
        }
        listOf(j1, j2).joinAll()

        visits.get().shouldBeExactly(10)
    }

    @Test
    suspend fun `tryLock times out when already held by another owner`() = coroutineScope {
        val lockName = "rethis:lock:timeout:${UUID.randomUUID()}"
        val owner1 = client.reHierarchicalDistributedLock(lockName)
        val owner2 = createClient().reHierarchicalDistributedLock(lockName)

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
        val owner1 = client.reHierarchicalDistributedLock(lockName)
        val owner2 = createClient().reHierarchicalDistributedLock(lockName)

        owner1.lock(2.seconds)  // Use longer lease to avoid expiry during test
        // While held, other owner should not acquire
        owner2.tryLock(waitTime = 100.milliseconds, leaseTime = 1.seconds).shouldBeFalse()
        // After release it should acquire quickly
        owner1.unlock().shouldBeTrue()
        val ok = owner2.tryLock(waitTime = 300.milliseconds, leaseTime = 1.seconds)
        ok.shouldBeTrue()
        owner2.unlock().shouldBeTrue()
    }

    @Test
    suspend fun `non-owner cannot unlock and does not delete the key`() = coroutineScope {
        val lockName = "rethis:lock:ownership:${UUID.randomUUID()}"
        val owner1 = client.reHierarchicalDistributedLock(lockName)
        val owner2 = createClient().reHierarchicalDistributedLock(lockName)

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
    suspend fun `same instance allows reentrant access from child coroutines`() = coroutineScope {
        val lockName = "rethis:lock:same-instance:${UUID.randomUUID()}"
        val shared = client.reHierarchicalDistributedLock(lockName)

        // Both j1 and j2 are children of this coroutineScope, so they share
        // the same "owner" identity and can both enter (reentrant behavior)
        var visits = 0

        val j1 = launch {
            repeat(5) {
                shared.lock(1.seconds)
                try {
                    delay(10)
                    visits += 1
                } finally {
                    shared.unlock().shouldBeTrue()
                }
            }
        }
        val j2 = launch {
            repeat(5) {
                shared.lock(5.seconds)  // Longer lease to avoid expiry under load
                try {
                    delay(10)
                    visits += 1
                } finally {
                    shared.unlock().shouldBeTrue()
                }
            }
        }
        listOf(j1, j2).joinAll()
        visits.shouldBeExactly(10)
    }

    @Test
    suspend fun `same key with separate lock instances blocks across coroutines`() = coroutineScope {
        val lockName = "rethis:lock:same-instance:${UUID.randomUUID()}"

        val inCritical = AtomicBoolean(false)
        var visits = 0

        val j1 = launch {
            // Each coroutine creates its own lock instance → different referenceJob → different owner
            val lock = client.reHierarchicalDistributedLock(lockName)
            repeat(5) {
                lock.lock(5.seconds)  // Longer lease
                try {
                    val first = inCritical.compareAndSet(false, true)
                    if (!first) error("Overlap detected!")
                    delay(40)
                    visits += 1
                } finally {
                    inCritical.set(false)
                    lock.unlock().shouldBeTrue()
                }
            }
        }
        val j2 = launch {
            val lock = client.reHierarchicalDistributedLock(lockName)
            repeat(5) {
                lock.lock(5.seconds)  // Longer lease
                try {
                    val first = inCritical.compareAndSet(false, true)
                    if (!first) error("Overlap detected!")
                    delay(40)
                    visits += 1
                } finally {
                    inCritical.set(false)
                    lock.unlock().shouldBeTrue()
                }
            }
        }
        listOf(j1, j2).joinAll()
        visits.shouldBeExactly(10)
    }

    @Test
    suspend fun `withLock releases on cancellation`() = coroutineScope {
        val lockName = "rethis:lock:cancel:${UUID.randomUUID()}"
        val lock = client.reHierarchicalDistributedLock(lockName)

        val entered = CompletableDeferred<Unit>()
        val willSuspend = CompletableDeferred<Unit>()

        val job = launch {
            lock.withLock(2.seconds) {
                // Signal that we are inside critical section (lock is held)
                entered.complete(Unit)
                // Wait here until the test tells us it's time to cancel
                willSuspend.await()
            }
        }

        // Wait until the lock is actually acquired and block entered
        entered.await()

        // Now cancel; finally { unlock() } runs in NonCancellable
        job.cancel()
        // Unblock the body so finally can execute promptly
        willSuspend.complete(Unit)
        job.join()

        // Give the watchdog a tiny window to observe unlock completion (usually not needed,
        // but helps on heavily loaded CI). Alternatively, poll Redis key absence.
        // delay(20)

        // Now we should be able to acquire
        lock.tryLock(waitTime = 500.milliseconds, leaseTime = 1.seconds).shouldBeTrue()
        lock.unlock().shouldBeTrue()
    }
}
