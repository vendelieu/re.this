package eu.vendeli.rethis.utils

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.server.info
import eu.vendeli.rethis.utils.LockUtils.ACQUIRE_LUA
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.test.config.DefaultTestConfig
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * Contention behaviour of the distributed lock: waiting for the release notification instead of spinning.
 */
class RedisLockContentionTest : ReThisTestCtx() {
    init {
        defaultTestConfig = DefaultTestConfig(timeout = 60.seconds)
    }

    private suspend fun ReThis.evalCalls(): Int =
        Regex("cmdstat_eval:calls=(\\d+)")
            .find(info("commandstats"))
            ?.groupValues
            ?.get(1)
            ?.toInt() ?: 0

    private fun lockName(tag: String) = "rethis:lock:contention:$tag:${UUID.randomUUID()}"

    @Test
    suspend fun `contended tryLock issues a bounded number of acquire attempts`() = withTimeout(30.seconds) {
        val name = lockName("no-spin")
        val holder = client.reDistributedLock(name)
        val waiterClient = createClient()
        val waiter = waiterClient.reDistributedLock(name, waitTime = 50.milliseconds)

        holder.lock(10.seconds)
        try {
            val before = client.evalCalls()
            waiter.tryLock(waitTime = 500.milliseconds, leaseTime = 5.seconds) shouldBe false
            val attempts = client.evalCalls() - before

            // 500ms / 50ms backoff plus the initial attempt, the post-subscribe retry and slack
            attempts shouldBeLessThanOrEqual 30
        } finally {
            holder.unlock()
        }
    }

    @Test
    suspend fun `waiter acquires promptly after release without waiting for the fallback delay`() =
        withTimeout(30.seconds) {
            val name = lockName("prompt")
            val holder = client.reDistributedLock(name)
            val waiter = createClient().reDistributedLock(name, waitTime = 5.seconds)

            holder.lock(10.seconds)
            coroutineScope {
                val waiting = async { waiter.tryLock(waitTime = 15.seconds, leaseTime = 5.seconds) }
                delay(500.milliseconds)

                val releasedAt = TimeSource.Monotonic.markNow()
                holder.unlock()
                waiting.await() shouldBe true
                releasedAt.elapsedNow() shouldBeLessThan 2.seconds
            }
            waiter.unlock()
        }

    @Test
    suspend fun `waiter still acquires via the fallback delay when no notification is published`() =
        withTimeout(30.seconds) {
            val name = lockName("fallback")
            val waiter = createClient().reDistributedLock(name, waitTime = 100.milliseconds)

            // The lease expires on its own, so no release notification is ever published.
            client.evalAsInt(ACQUIRE_LUA, arrayOf(name), listOf("foreign-owner", "400")) shouldBeExactly 1

            val started = TimeSource.Monotonic.markNow()
            waiter.tryLock(waitTime = 5.seconds, leaseTime = 5.seconds) shouldBe true
            val elapsed = started.elapsedNow()

            elapsed shouldBeGreaterThan 300.milliseconds
            elapsed shouldBeLessThanOrEqualTo 2.seconds
            waiter.unlock()
        }

    @Test
    suspend fun `tryLock keeps at most one subscription and releases it on timeout success and cancellation`() =
        withTimeout(40.seconds) {
            val name = lockName("subscriptions")
            val holder = client.reDistributedLock(name)
            val waiterClient = createClient()
            val waiter = waiterClient.reDistributedLock(name, waitTime = 50.milliseconds)

            holder.lock(20.seconds)

            // timeout
            coroutineScope {
                val observed = async {
                    var peak = 0
                    repeat(20) {
                        peak =
                            maxOf(
                                peak,
                                waiterClient.subscriptions.activeSubscriptions.values
                                    .sumOf { it.handlers.size },
                            )
                        delay(20.milliseconds)
                    }
                    peak
                }
                waiter.tryLock(waitTime = 600.milliseconds, leaseTime = 5.seconds) shouldBe false
                observed.await() shouldBeLessThanOrEqual 1
            }
            eventually(5.seconds) { waiterClient.subscriptions.size shouldBeExactly 0 }

            // cancellation
            coroutineScope {
                val waiting = launch { waiter.tryLock(waitTime = 15.seconds, leaseTime = 5.seconds) }
                eventually(5.seconds) { waiterClient.subscriptions.size shouldBeExactly 1 }
                waiting.cancelAndJoin()
            }
            eventually(5.seconds) { waiterClient.subscriptions.size shouldBeExactly 0 }

            // success
            coroutineScope {
                val waiting = async { waiter.tryLock(waitTime = 15.seconds, leaseTime = 5.seconds) }
                eventually(5.seconds) { waiterClient.subscriptions.size shouldBeExactly 1 }
                holder.unlock()
                waiting.await() shouldBe true
            }
            eventually(5.seconds) { waiterClient.subscriptions.size shouldBeExactly 0 }
            waiter.unlock()
        }

    private suspend fun ReThis.connectedClients(): Int =
        Regex("connected_clients:(\\d+)")
            .find(info("clients"))
            ?.groupValues
            ?.get(1)
            ?.toInt() ?: 0

    @Test
    suspend fun `many short tryLock calls leave no subscriptions or connections behind`() = withTimeout(40.seconds) {
        val name = lockName("short-waits")
        val holder = client.reDistributedLock(name)
        val waiterClient = createClient()
        val waiter = waiterClient.reDistributedLock(name, waitTime = 50.milliseconds)

        holder.lock(30.seconds)
        try {
            waiter.tryLock(waitTime = 100.milliseconds, leaseTime = 5.seconds) shouldBe false // warm up the pool
            eventually(5.seconds) { waiterClient.subscriptions.size shouldBeExactly 0 }
            val baseline = client.connectedClients()

            // 500 calls with a wait too short for the subscription to settle, some of them concurrent
            coroutineScope {
                List(20) {
                    async(Dispatchers.Default) {
                        val own = waiterClient.reDistributedLock(name, waitTime = 50.milliseconds)
                        repeat(25) { own.tryLock(waitTime = 2.milliseconds, leaseTime = 5.seconds) shouldBe false }
                    }
                }.forEach { it.await() }
            }

            // nothing is ever published, so only an explicit release can free the workers
            delay(1.seconds)
            eventually(10.seconds) {
                waiterClient.subscriptions.size shouldBeExactly 0
                client.connectedClients() shouldBeLessThanOrEqual baseline
            }
        } finally {
            holder.unlock()
        }
    }

    @Test
    suspend fun `concurrent waiters on one client acquire in turn and leave nothing behind`() =
        withTimeout(40.seconds) {
            val name = lockName("concurrent")
            val holder = client.reDistributedLock(name)
            val waiterClient = createClient()
            val acquired = AtomicInteger(0)

            holder.lock(30.seconds)
            coroutineScope {
                val waiters = List(3) {
                    async {
                        val lock = waiterClient.reDistributedLock(name, waitTime = 8.seconds)
                        lock.tryLock(waitTime = 20.seconds, leaseTime = 10.seconds) shouldBe true
                        acquired.incrementAndGet()
                        lock.unlock()
                    }
                }
                // a waiter that gives up early must not disturb the others
                val leaver = waiterClient.reDistributedLock(name, waitTime = 8.seconds)
                eventually(5.seconds) { waiterClient.subscriptions.size shouldBeExactly 1 }
                leaver.tryLock(waitTime = 200.milliseconds, leaseTime = 5.seconds) shouldBe false
                delay(300.milliseconds)

                val releasedAt = TimeSource.Monotonic.markNow()
                holder.unlock()
                waiters.forEach { it.await() }

                acquired.get() shouldBeExactly 3
                releasedAt.elapsedNow() shouldBeLessThan 6.seconds // each waiter wakes by notification, not fallback
            }
            eventually(5.seconds) { waiterClient.subscriptions.size shouldBeExactly 0 }
        }
}
