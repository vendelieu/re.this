package eu.vendeli.rethis.topology

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.configuration.ClusterConfiguration
import eu.vendeli.rethis.types.common.Address
import eu.vendeli.rethis.types.common.RespVer
import eu.vendeli.rethis.utils.withRetry
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.io.IOException
import kotlin.time.Duration.Companion.milliseconds

class TopologyRefreshTest : TestCtx() {
    private val logger = ClusterConfiguration(RespVer.V3).loggerFactory.get("test")

    private fun cfg(times: Int) = ClusterConfiguration(RespVer.V3).apply {
        retry {
            this.times = times
            initialDelay = 1.milliseconds
            maxDelay = 1.milliseconds
        }
    }

    private suspend fun <T> pickedSeeds(seeds: List<T>, times: Int): List<T> {
        val picked = mutableListOf<T>()
        shouldThrow<IOException> {
            withRetry(cfg(times)) { attempt ->
                picked += seeds.seedFor(attempt)
                throw IOException("down")
            }
        }
        return picked
    }

    @Test
    fun `single seed is reused for every attempt without suppressed failures`() = runBlocking {
        val seed = Address("10.0.0.1", 7000)
        var thrown: Throwable? = null
        val picked = mutableListOf<Address>()
        try {
            withRetry(cfg(3)) { attempt ->
                picked += listOf(seed).seedFor(attempt)
                throw IOException("down")
            }
        } catch (e: IOException) {
            thrown = e
        }

        picked.shouldContainExactly(seed, seed, seed)
        thrown!!.suppressed.none { it is NoSuchElementException } shouldBe true
    }

    @Test
    fun `seeds alternate across attempts`() = runBlocking {
        pickedSeeds(listOf("A", "B"), 3).shouldContainExactly("A", "B", "A")
    }

    @Test
    fun `failed refresh does not cancel the scope and a later refresh succeeds`() = runBlocking {
        val scope = CoroutineScope(Job())
        var calls = 0

        val results = mutableListOf<Boolean>()
        scope
            .launch {
                results += runRefreshSafely(logger) {
                    calls++
                    error("seed down")
                }
                results += runRefreshSafely(logger) { calls++ }
            }.join()

        results.shouldContainExactly(false, true)
        calls shouldBe 2
        scope.coroutineContext[Job]!!.isActive shouldBe true
    }

    @Test
    fun `cancellation is not swallowed`() = runBlocking {
        shouldThrow<CancellationException> {
            runRefreshSafely(logger) { throw CancellationException("cancelled") }
        }
        Unit
    }

    @Test
    fun `timeout is treated as a failed refresh`() = runBlocking {
        runRefreshSafely(logger) { withTimeout(1) { delay(1_000) } } shouldBe false
    }
}
