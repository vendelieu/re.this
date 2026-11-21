package eu.vendeli.rethis.topology

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.configuration.ClusterConfiguration
import eu.vendeli.rethis.core.ConnectionFactory
import eu.vendeli.rethis.types.common.Address
import eu.vendeli.rethis.types.common.RespVer
import eu.vendeli.rethis.utils.withRetry
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.io.IOException
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ClusterSeedFailoverTest : TestCtx() {
    @Test
    fun `unavailable connection is retried for every attempt`() = runBlocking {
        val cfg = ClusterConfiguration(RespVer.V3).apply {
            maxConnections = 1
            retry {
                times = 3
                initialDelay = 1.milliseconds
                maxDelay = 1.milliseconds
            }
        }
        val factory = ConnectionFactory(cfg, Job())
        FakeRespServer().use { server ->
            // the only permit is taken, so every further attempt gets no connection
            val held = factory.createConnOrNull(server.address.socket)!!
            var calls = 0

            shouldThrow<IOException> {
                withRetry(cfg) {
                    calls++
                    factory.createConnOrIoError(server.address.socket)
                }
            }

            calls shouldBe 3
            factory.dispose(held)
        }
    }

    @Test
    fun `slot fetch moves on to the next seed when a seed cannot be connected`() = runBlocking {
        FakeRespServer().use { live ->
            val unreachable = Address("127.0.0.1", 1)
            val client = ReThis.cluster(listOf(unreachable, live.address)) {
                retry {
                    times = 3
                    initialDelay = 10.milliseconds
                    maxDelay = 10.milliseconds
                }
            }
            try {
                withTimeout(10.seconds) {
                    while (live.commandCount("CLUSTER", "SLOTS") == 0) delay(25.milliseconds)
                }
                live.commandCount("CLUSTER", "SLOTS") shouldBe 1
            } finally {
                client.close()
            }
        }
    }
}
