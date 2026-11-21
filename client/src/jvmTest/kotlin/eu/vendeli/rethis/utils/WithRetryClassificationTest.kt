package eu.vendeli.rethis.utils

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.configuration.StandaloneConfiguration
import eu.vendeli.rethis.shared.types.ClusterException
import eu.vendeli.rethis.shared.types.CommandTimeoutException
import eu.vendeli.rethis.shared.types.CrossSlotOperationException
import eu.vendeli.rethis.shared.types.KeyAbsentException
import eu.vendeli.rethis.shared.types.ReThisException
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.topology.ClusterNotReadyException
import eu.vendeli.rethis.types.common.RespVer
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class WithRetryClassificationTest : TestCtx() {
    private val times = 3

    private fun cfg() = StandaloneConfiguration(RespVer.V3).apply {
        retry {
            this.times = this@WithRetryClassificationTest.times
            initialDelay = 10.seconds
            maxDelay = 10.seconds
        }
    }

    private fun retryable(operation: RedisOperation): (Throwable) -> Boolean =
        { it.isRetryableFor(operation) }

    @Test
    fun `cancellation is rethrown immediately without a delay`() = runTest {
        var calls = 0
        shouldThrow<CancellationException> {
            withRetry(cfg()) {
                calls++
                throw CancellationException("cancelled")
            }
        }
        calls shouldBe 1
        currentTime shouldBe 0
    }

    @Test
    fun `redis error reply is not retried`() = runTest {
        var calls = 0
        shouldThrow<ReThisException> {
            withRetry(cfg()) {
                calls++
                throw ReThisException("WRONGTYPE Operation against a key holding the wrong kind of value")
            }
        }
        calls shouldBe 1
    }

    @Test
    fun `io failure is retried for every attempt with backoff`() = runTest {
        var calls = 0
        shouldThrow<IOException> {
            withRetry(cfg()) {
                calls++
                throw IOException("reset")
            }
        }
        calls shouldBe times
        currentTime shouldBe 20_000
    }

    @Test
    fun `timeout is retried for a read`() = runTest {
        var calls = 0
        shouldThrow<CommandTimeoutException> {
            withRetry(cfg(), retryable(RedisOperation.READ)) {
                calls++
                throw CommandTimeoutException("slow")
            }
        }
        calls shouldBe times
    }

    @Test
    fun `timeout is surfaced without a resend for a write`() = runTest {
        var calls = 0
        shouldThrow<CommandTimeoutException> {
            withRetry(cfg(), retryable(RedisOperation.WRITE)) {
                calls++
                throw CommandTimeoutException("slow")
            }
        }
        calls shouldBe 1
    }

    @Test
    fun `exhausted attempts throw the first failure with the later ones suppressed`() = runTest {
        val failures = List(times) { IOException("failure $it") }
        var calls = 0
        val thrown = shouldThrow<IOException> {
            withRetry(cfg()) { throw failures[calls++] }
        }
        (thrown === failures.first()) shouldBe true
        thrown.suppressed.toList().shouldContainExactly(failures.drop(1))
    }

    @Test
    fun `a cluster that is not ready yet is retried while the other cluster errors stay permanent`() = runTest {
        ClusterNotReadyException().isTransientFailure() shouldBe true
        ClusterException("No master for slot 9000").isTransientFailure() shouldBe false
        CrossSlotOperationException("CROSSSLOT").isTransientFailure() shouldBe false
        KeyAbsentException("no key").isTransientFailure() shouldBe false

        var calls = 0
        shouldThrow<ClusterNotReadyException> {
            withRetry(cfg(), retryable(RedisOperation.WRITE)) {
                calls++
                throw ClusterNotReadyException()
            }
        }
        calls shouldBe times
    }
}
