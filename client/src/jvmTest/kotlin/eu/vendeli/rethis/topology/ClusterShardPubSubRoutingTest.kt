package eu.vendeli.rethis.topology

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.command.pubsub.sPublish
import eu.vendeli.rethis.command.pubsub.sSubscribe
import eu.vendeli.rethis.shared.utils.CRC16
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ClusterShardPubSubRoutingTest : TestCtx() {
    private val firstRange = 0..5460
    private val secondRange = 5461..10922
    private val thirdRange = 10923..16383

    private val thirdChannel = generateSequence(0) { it + 1 }
        .map { "shard$it" }
        .first { CRC16.slot(it.toByteArray()) in thirdRange }

    private fun withCluster(block: suspend (ReThis, List<ScriptedClusterNode>) -> Unit) {
        val nodes = List(3) {
            ScriptedClusterNode(override = { cmd ->
                if (cmd.first() ==
                    "SPUBLISH"
                ) ":0\r\n" else null
            })
        }
        var client: ReThis? = null
        try {
            val reply = clusterSlotsReply(
                SlotBlock(firstRange, nodes[0].address),
                SlotBlock(secondRange, nodes[1].address),
                SlotBlock(thirdRange, nodes[2].address),
            )
            nodes.forEach { it.slotsReply = reply }
            client = ReThis.cluster(listOf(nodes.first().address)) { movedBackoffPeriod = 10.milliseconds }
            runBlocking { withTimeout(20.seconds) { block(client, nodes) } }
        } finally {
            client?.close()
            nodes.forEach { it.close() }
        }
    }

    @Test
    fun `SPUBLISH goes to the node owning the slot of the shard channel`() = withCluster { client, nodes ->
        client.sPublish(thirdChannel, "payload") shouldBe 0L

        nodes.map { it.commandCount("SPUBLISH") } shouldBe listOf(0, 0, 1)
    }

    @Test
    fun `SSUBSCRIBE goes to the node owning the slot of the shard channel`() = withCluster { client, nodes ->
        client.sSubscribe(thirdChannel) { _, _: String -> }

        eventually(5.seconds) { nodes.map { it.commandCount("SSUBSCRIBE") } shouldBe listOf(0, 0, 1) }
    }
}
