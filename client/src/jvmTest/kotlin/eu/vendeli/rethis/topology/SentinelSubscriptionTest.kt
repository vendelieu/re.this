package eu.vendeli.rethis.topology

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.types.common.Address
import eu.vendeli.rethis.types.common.ReadFrom
import eu.vendeli.rethis.types.common.RespVer
import io.kotest.matchers.shouldBe
import io.ktor.network.sockets.InetSocketAddress
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

class SentinelSubscriptionTest : TestCtx() {
    private val masterQuery = arrayOf("SENTINEL", "GET-MASTER-ADDR-BY-NAME")

    private suspend fun awaitUntil(condition: suspend () -> Boolean) = withTimeout(15.seconds) {
        while (!condition()) delay(25.milliseconds)
    }

    /** Samples [read] until it has stopped changing for [quiet], and returns that value. */
    private suspend fun awaitStable(
        quiet: Duration = 500.milliseconds,
        read: () -> Int,
    ): Int = withTimeout(15.seconds) {
        var last = read()
        var since = TimeSource.Monotonic.markNow()
        while (since.elapsedNow() < quiet) {
            delay(25.milliseconds)
            val now = read()
            if (now != last) {
                last = now
                since = TimeSource.Monotonic.markNow()
            }
        }
        last
    }

    private fun switchMessage(newMaster: Address) = (newMaster.socket as InetSocketAddress).let {
        "mymaster 10.0.0.1 6379 ${it.hostname} ${it.port}"
    }

    @Test
    fun `one failover announced by every sentinel triggers a single refresh`() = runBlocking {
        FakeRespServer().use { oldMaster ->
            FakeRespServer().use { newMaster ->
                val sentinels =
                    List(3) { FakeRespServer(masterReply = FakeRespServer.masterAddrReply(oldMaster.address)) }
                try {
                    val client = ReThis.sentinel("mymaster", sentinels.map { it.address }, RespVer.V2) {
                        pool {
                            minIdleConnections =
                                0
                        }
                    }
                    try {
                        awaitUntil { sentinels.all { "+switch-master" in it.subscribedChannels() } }
                        awaitUntil { sentinels.sumOf { it.commandCount(*masterQuery) } == 1 }
                        sentinels.forEach { it.masterReply = FakeRespServer.masterAddrReply(newMaster.address) }

                        sentinels.forEach { it.publish("+switch-master", switchMessage(newMaster.address)) }

                        awaitStable { sentinels.sumOf { it.commandCount(*masterQuery) } } shouldBe 2
                    } finally {
                        client.close()
                    }
                } finally {
                    sentinels.forEach { it.close() }
                }
            }
        }
    }

    @Test
    fun `a sentinel that is down at startup is subscribed once it comes up`() = runBlocking {
        val port = FakeRespServer().use { (it.address.socket as InetSocketAddress).port }
        val lateAddress = Address("127.0.0.1", port)
        FakeRespServer().use { dataNode ->
            FakeRespServer(masterReply = FakeRespServer.masterAddrReply(dataNode.address)).use { live ->
                val client = ReThis.sentinel("mymaster", listOf(live.address, lateAddress), RespVer.V2) {
                    connectionAcquireTimeout = 300.milliseconds
                    retry {
                        initialDelay = 50.milliseconds
                        maxDelay = 200.milliseconds
                    }
                }
                try {
                    awaitUntil { "+switch-master" in live.subscribedChannels() }

                    FakeRespServer(port = port).use { late ->
                        awaitUntil { "+switch-master" in late.subscribedChannels() }
                    }
                } finally {
                    client.close()
                }
            }
        }
    }

    @Test
    fun `a replica that never answers the latency probe does not stall the refresh`() = runBlocking {
        FakeRespServer().use { master ->
            FakeRespServer(silent = true).use { replica ->
                val replicas = FakeRespServer.array(
                    FakeRespServer.replicaEntry(
                        "127.0.0.1",
                        (replica.address.socket as InetSocketAddress).port,
                        "slave",
                    ),
                )
                FakeRespServer(FakeRespServer.masterAddrReply(master.address), replicas).use { sentinel ->
                    val client = ReThis.sentinel("mymaster", listOf(sentinel.address), RespVer.V2) {
                        readFromStrategy = ReadFrom.LowestLatency
                    }
                    try {
                        awaitUntil { runCatching { client.topology.route(readRequest()) }.isSuccess }
                    } finally {
                        client.close()
                    }
                }
            }
        }
    }

    private fun readRequest() = eu.vendeli.rethis.shared.types.CommandRequest(
        kotlinx.io.Buffer(),
        eu.vendeli.rethis.shared.types.RedisOperation.READ,
        command = "GET",
    )
}
