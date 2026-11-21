package eu.vendeli.rethis.topology

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.command.connection.ping
import eu.vendeli.rethis.command.generic.del
import eu.vendeli.rethis.command.scripting.eval
import eu.vendeli.rethis.command.scripting.fcall
import eu.vendeli.rethis.command.string.get
import eu.vendeli.rethis.command.string.mGet
import eu.vendeli.rethis.command.string.set
import eu.vendeli.rethis.configuration.ClusterConfiguration
import eu.vendeli.rethis.shared.types.CrossSlotOperationException
import eu.vendeli.rethis.shared.types.ReThisException
import eu.vendeli.rethis.shared.utils.CRC16
import eu.vendeli.rethis.types.common.Address
import eu.vendeli.rethis.types.common.ReadFrom
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ClusterSlotRoutingTest : TestCtx() {
    private val firstRange = 0..5460
    private val secondRange = 5461..10922
    private val thirdRange = 10923..16383

    private fun keyIn(range: IntRange, prefix: String = "k") =
        generateSequence(0) { it + 1 }.map { "$prefix$it" }.first { CRC16.slot(it.toByteArray()) in range }

    private val firstKey = keyIn(firstRange)
    private val secondKey = keyIn(secondRange)
    private val thirdKey = keyIn(thirdRange)

    private fun cluster(
        seed: Address,
        configurator: ClusterConfiguration.() -> Unit = {},
    ) = ReThis.cluster(listOf(seed)) {
        movedBackoffPeriod = 10.milliseconds
        configurator()
    }

    private inline fun withNodes(block: (List<ScriptedClusterNode>) -> Unit) {
        val nodes = List(3) { ScriptedClusterNode() }
        try {
            val reply = clusterSlotsReply(
                SlotBlock(firstRange, nodes[0].address),
                SlotBlock(secondRange, nodes[1].address),
                SlotBlock(thirdRange, nodes[2].address),
            )
            nodes.forEach { it.slotsReply = reply }
            block(nodes)
        } finally {
            nodes.forEach { it.close() }
        }
    }

    private fun inCluster(
        nodes: List<ScriptedClusterNode>,
        configurator: ClusterConfiguration.() -> Unit = {},
        block: suspend (ReThis) -> Unit,
    ) {
        val client = cluster(nodes.first().address, configurator)
        try {
            runBlocking { withTimeout(20.seconds) { block(client) } }
        } finally {
            client.close()
        }
    }

    @Test
    fun `commands go to the owner of the key slot and not to the first node`() = withNodes { nodes ->
        inCluster(nodes) { client ->
            client.set(thirdKey, "x")
            client.get(thirdKey)
            client.get(secondKey)
        }

        nodes.map { it.commandCount("SET") } shouldBe listOf(0, 0, 1)
        nodes.map { it.commandCount("GET") } shouldBe listOf(0, 1, 1)
        nodes[2].commandCount("GET", thirdKey) shouldBe 1
        nodes[1].commandCount("GET", secondKey) shouldBe 1
    }

    @Test
    fun `keyless commands are answered by exactly one node`() = withNodes { nodes ->
        inCluster(nodes) { client -> client.ping() shouldBe "PONG" }

        nodes.sumOf { it.commandCount("PING") } shouldBe 1
    }

    @Test
    fun `keys sharing a hash tag are accepted in one command and reach the slot owner`() = withNodes { nodes ->
        val tag = "{$thirdKey}"
        inCluster(nodes) { client ->
            client.mGet("${tag}a", "${tag}b") shouldBe listOf("v:${tag}a", "v:${tag}b")
            client.del("${tag}a", "${tag}b") shouldBe 2L
        }

        nodes.map { it.commandCount("MGET") } shouldBe listOf(0, 0, 1)
        nodes.map { it.commandCount("DEL") } shouldBe listOf(0, 0, 1)
    }

    @Test
    fun `keys of different slots are rejected client side naming the slots`() = withNodes { nodes ->
        inCluster(nodes) { client ->
            val e = shouldThrow<CrossSlotOperationException> { client.mGet(firstKey, thirdKey) }
            e.message shouldContain CRC16.slot(firstKey.toByteArray()).toString()
            e.message shouldContain CRC16.slot(thirdKey.toByteArray()).toString()
            shouldThrow<CrossSlotOperationException> { client.del("{a}1", "{b}1") }
            shouldThrow<CrossSlotOperationException> { client.mGet("a", "b{}") }
        }

        nodes.sumOf { it.commandCount("MGET") + it.commandCount("DEL") } shouldBe 0
    }

    @Test
    fun `moved redirect makes the following commands go to the new owner`() = withNodes { nodes ->
        // the topology still says the first node owns the key, while the key has moved to the second one
        val movedTo = nodes[1].address
        nodes[0].override = { cmd ->
            if (cmd.first() == "GET") "-MOVED ${CRC16.slot(cmd[1].toByteArray())} ${movedTo.hostPort()}\r\n" else null
        }

        inCluster(nodes) { client ->
            client.get(firstKey) shouldBe "v:$firstKey"
            client.get(firstKey) shouldBe "v:$firstKey"
        }

        nodes[0].commandCount("GET") shouldBe 1
        nodes[1].commandCount("GET") shouldBe 2
    }

    @Test
    fun `ask redirect is served by the named node and leaves later commands on the owner`() = withNodes { nodes ->
        val askTarget = nodes[1].address
        var asked = false
        nodes[0].override = { cmd ->
            if (cmd.first() == "GET" && !asked) {
                asked = true
                "-ASK ${CRC16.slot(cmd[1].toByteArray())} ${askTarget.hostPort()}\r\n"
            } else {
                null
            }
        }

        inCluster(nodes) { client ->
            client.get(firstKey) shouldBe "v:$firstKey"
            client.get(firstKey) shouldBe "v:$firstKey"
        }

        nodes[0].commandCount("GET") shouldBe 2
        nodes[1].commandCount("ASKING") shouldBe 1
        nodes[1].commandCount("GET") shouldBe 1
    }

    @Test
    fun `ask answered with moved follows the move to the new owner`() = withNodes { nodes ->
        nodes[0].override = { cmd ->
            if (cmd.first() ==
                "GET"
            ) "-ASK ${CRC16.slot(cmd[1].toByteArray())} ${nodes[1].address.hostPort()}\r\n" else null
        }
        nodes[1].override = { cmd ->
            if (cmd.first() ==
                "GET"
            ) "-MOVED ${CRC16.slot(cmd[1].toByteArray())} ${nodes[2].address.hostPort()}\r\n" else null
        }

        inCluster(nodes) { client -> client.get(firstKey) shouldBe "v:$firstKey" }

        nodes.map { it.commandCount("GET") } shouldBe listOf(1, 1, 1)
    }

    @Test
    fun `scripts without keys run on a single node`() = withNodes { nodes ->
        inCluster(nodes) { client ->
            client.eval("return 1", arg = emptyList())
            client.fcall("fn", arg = emptyList())
        }

        nodes.sumOf { it.commandCount("EVAL") } shouldBe 1
        nodes.sumOf { it.commandCount("FCALL") } shouldBe 1
    }

    @Test
    fun `script keys decide the node and must share a slot`() = withNodes { nodes ->
        inCluster(nodes) { client ->
            client.eval("return 1", thirdKey, arg = emptyList())
            shouldThrow<CrossSlotOperationException> { client.eval("return 1", firstKey, thirdKey, arg = emptyList()) }
        }

        nodes.map { it.commandCount("EVAL") } shouldBe listOf(0, 0, 1)
    }

    @Test
    fun `reads from replicas go to a replica of the slot master and writes stay on the master`() {
        val nodes = List(3) { ScriptedClusterNode() }
        val replica = ScriptedClusterNode()
        try {
            val reply = clusterSlotsReply(
                SlotBlock(firstRange, nodes[0].address),
                SlotBlock(secondRange, nodes[1].address),
                SlotBlock(thirdRange, nodes[2].address, listOf(replica.address)),
            )
            (nodes + replica).forEach { it.slotsReply = reply }
            replica.replicaOf = nodes[2].address

            inCluster(nodes, { readFromStrategy = ReadFrom.Replica }) { client ->
                client.get(thirdKey) shouldBe "v:$thirdKey"
                client.set(thirdKey, "x")
            }

            replica.commandCount("GET") shouldBe 1
            replica.commandCount("SET") shouldBe 0
            nodes.sumOf { it.commandCount("GET") } shouldBe 0
            nodes[2].commandCount("SET") shouldBe 1
        } finally {
            (nodes + replica).forEach { it.close() }
        }
    }

    @Test
    fun `slot with no replica fails fast under strict replica reads`() = withNodes { nodes ->
        inCluster(nodes, { readFromStrategy = ReadFrom.Replica }) { client ->
            shouldThrow<ReThisException> { client.get(firstKey) }
        }

        nodes.sumOf { it.commandCount("GET") } shouldBe 0
    }
}
