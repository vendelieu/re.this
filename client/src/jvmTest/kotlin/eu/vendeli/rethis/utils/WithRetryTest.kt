package eu.vendeli.rethis.utils

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.configuration.StandaloneConfiguration
import eu.vendeli.rethis.core.ConnectionFactory
import eu.vendeli.rethis.topology.FakeRespServer
import eu.vendeli.rethis.types.common.RespVer
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlinx.io.readString
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class WithRetryTest : TestCtx() {
    private fun cfg(times: Int) = StandaloneConfiguration(RespVer.V3).apply {
        retry {
            this.times = times
            initialDelay = 100.milliseconds
            maxDelay = 100.milliseconds
        }
    }

    @Test
    fun `makes exactly N attempts for every N`() = runTest {
        for (times in 1..4) {
            var calls = 0
            shouldThrow<IOException> {
                withRetry(cfg(times)) {
                    calls++
                    throw IOException("boom")
                }
            }
            calls shouldBe times
        }
    }

    @Test
    fun `does not sleep after the last failed attempt`() = runTest {
        shouldThrow<IOException> { withRetry(cfg(1)) { throw IOException("boom") } }
        currentTime shouldBe 0

        shouldThrow<IOException> { withRetry(cfg(3)) { throw IOException("boom") } }
        // two back-offs between three attempts, none after the last one
        currentTime shouldBe 200
    }

    @Test
    fun `returns the result of a late success`() = runTest {
        var calls = 0
        withRetry(cfg(3)) { if (calls++ < 2) throw IOException("boom") else "ok" } shouldBe "ok"
        calls shouldBe 3
    }

    @Test
    fun `keeps the first failure and suppresses the later ones in order`() = runTest {
        val failures = List(3) { IOException("boom $it") }
        var calls = 0

        val thrown = shouldThrow<IOException> { withRetry(cfg(3)) { throw failures[calls++] } }

        (thrown === failures.first()) shouldBe true
        thrown.suppressed.toList().shouldContainExactly(failures.drop(1))
    }

    @Test
    fun `request payload is resent in full on every attempt`() = runBlocking {
        val clientCfg = StandaloneConfiguration(RespVer.V2)
        val frame = "*1\r\n\$4\r\nPING\r\n"
        val payload = Buffer().apply { write(frame.encodeToByteArray()) }

        FakeRespServer().use { server ->
            val conn = ConnectionFactory(clientCfg, Job()).createConnOrNull(server.address.socket)!!
            repeat(2) { conn.doRequest(payload, commandTimeout = 5.seconds) }

            server.commands.shouldContainExactly(listOf("PING"), listOf("PING"))
            payload.copy().readString() shouldBe frame
        }
    }
}
