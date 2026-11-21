package eu.vendeli.rethis.topology

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.command.string.get
import eu.vendeli.rethis.command.transaction.watch
import eu.vendeli.rethis.shared.utils.CRC16
import eu.vendeli.rethis.types.common.RespVer
import io.kotest.matchers.shouldBe
import io.ktor.network.sockets.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

class PinnedConnectionRoutingTest : TestCtx() {
    private val ranges = listOf(0..5460, 5461..10922, 10923..16383)

    private fun keyIn(range: IntRange) =
        generateSequence(0) { it + 1 }.map { "k$it" }.first { CRC16.slot(it.toByteArray()) in range }

    @Test
    fun `cluster scope is pinned to the master owning the key slot`() {
        val nodes = List(3) { ScriptedClusterNode() }
        try {
            val reply = clusterSlotsReply(*ranges.mapIndexed { i, r -> SlotBlock(r, nodes[i].address) }.toTypedArray())
            nodes.forEach { it.slotsReply = reply }
            val client = ReThis.cluster(listOf(nodes.first().address))
            try {
                val keys = ranges.map(::keyIn)
                runBlocking {
                    withTimeout(20.seconds) {
                        client.withConnection(keys[2]) {
                            watch(keys[2])
                            // pinned: this key belongs to another node, but the command stays on the pinned one
                            get(keys[0])
                        }
                    }
                }

                nodes.map { it.commandCount("WATCH") } shouldBe listOf(0, 0, 1)
                nodes.map { it.commandCount("GET") } shouldBe listOf(0, 0, 1)
                nodes.map { it.commandCount("UNWATCH") } shouldBe listOf(0, 0, 1)
            } finally {
                client.close()
            }
        } finally {
            nodes.forEach { it.close() }
        }
    }

    @Test
    fun `UNWATCH is sent after the block completes normally and after it throws`() {
        FakeRespServer().use { server ->
            val socket = server.address.socket as InetSocketAddress
            val client = ReThis(socket.hostname, socket.port, RespVer.V2) { pool { setClientName = false } }
            try {
                runBlocking {
                    client.withConnection { watch("someKey") }
                    server.commands.map { it.first() } shouldBe listOf("WATCH", "UNWATCH")

                    runCatching { client.withConnection<Unit> { error("boom") } }
                    // a failed block drops the connection instead of reusing it
                    server.commandCount("UNWATCH") shouldBe 1
                }
            } finally {
                client.close()
            }
        }
    }
}
