package eu.vendeli.rethis.utils

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.configuration.StandaloneConfiguration
import eu.vendeli.rethis.types.common.RespVer
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import kotlinx.io.readString
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class WithRetryTest : TestCtx() {
    private fun cfg(times: Int) = StandaloneConfiguration(RespVer.V3).apply {
        retry {
            this.times = times
            initialDelay = 100.milliseconds
            maxDelay = 100.milliseconds
        }
    }

    @Test
    fun `makes exactly N attempts for every N`() = runBlocking {
        for (times in 1..4) {
            var calls = 0
            shouldThrow<IllegalStateException> {
                withRetry(cfg(times)) {
                    calls++
                    error("boom")
                }
            }
            calls shouldBe times
        }
    }

    @Test
    fun `does not sleep after the last failed attempt`() = runBlocking {
        val start = TimeSource.Monotonic.markNow()
        shouldThrow<IllegalStateException> { withRetry(cfg(1)) { error("boom") } }
        (start.elapsedNow() < 90.milliseconds) shouldBe true
    }

    @Test
    fun `returns first successful result and keeps suppressed failures`() = runBlocking {
        var calls = 0
        withRetry(cfg(3)) { if (calls++ < 2) error("boom") else "ok" } shouldBe "ok"
        calls shouldBe 3
    }

    @Test
    fun `payload copy keeps request replayable`() {
        val payload = Buffer().apply { write("PING".encodeToByteArray()) }
        val sink = Buffer()
        repeat(2) { sink.write(payload.copy(), payload.size) }
        sink.readString() shouldBe "PINGPING"
    }
}
