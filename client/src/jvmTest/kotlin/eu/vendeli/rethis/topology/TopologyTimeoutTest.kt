package eu.vendeli.rethis.topology

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.TestCtx
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class TopologyTimeoutTest : TestCtx() {
    private val commandTimeout = 300.milliseconds
    private val waitBound = 10.seconds

    private suspend fun awaitUntil(condition: () -> Boolean) = withTimeout(waitBound) {
        while (!condition()) delay(25.milliseconds)
    }

    @Test
    fun `cluster slot fetch gives up on a silent seed after the command timeout`() = runBlocking {
        FakeRespServer(silent = true).use { silent ->
            FakeRespServer().use { live ->
                val client = ReThis.cluster(listOf(silent.address, live.address)) {
                    this.commandTimeout = this@TopologyTimeoutTest.commandTimeout
                    retry {
                        times = 2
                        initialDelay = 1.milliseconds
                        maxDelay = 1.milliseconds
                    }
                }
                try {
                    awaitUntil { live.commandCount("CLUSTER", "SLOTS") == 1 }
                    silent.commandCount("CLUSTER", "SLOTS") shouldBe 1
                } finally {
                    client.close()
                }
            }
        }
    }

    @Test
    fun `sentinel topology fetch gives up on a silent sentinel after the command timeout`() = runBlocking {
        FakeRespServer().use { dataNode ->
            FakeRespServer(silent = true).use { silent ->
                FakeRespServer(masterReply = FakeRespServer.masterAddrReply(dataNode.address)).use { live ->
                    val client = ReThis.sentinel("mymaster", listOf(silent.address, live.address)) {
                        this.commandTimeout = this@TopologyTimeoutTest.commandTimeout
                    }
                    try {
                        awaitUntil { live.commandCount("SENTINEL", "GET-MASTER-ADDR-BY-NAME") == 1 }
                        silent.commandCount("SENTINEL", "GET-MASTER-ADDR-BY-NAME") shouldBe 1
                    } finally {
                        client.close()
                    }
                }
            }
        }
    }

    @Test
    fun `without a command timeout a silent seed keeps the fetch waiting`() = runBlocking {
        FakeRespServer(silent = true).use { silent ->
            FakeRespServer().use { live ->
                val client = ReThis.cluster(listOf(silent.address, live.address)) {
                    retry {
                        times = 2
                        initialDelay = 1.milliseconds
                        maxDelay = 1.milliseconds
                    }
                }
                try {
                    awaitUntil { silent.commandCount("CLUSTER", "SLOTS") == 1 }
                    delay(commandTimeout * 3)

                    live.commandCount("CLUSTER", "SLOTS") shouldBe 0
                } finally {
                    client.close()
                }
            }
        }
    }
}
