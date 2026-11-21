package eu.vendeli.rethis.topology

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.command.connection.ping
import eu.vendeli.rethis.command.string.get
import eu.vendeli.rethis.command.string.set
import eu.vendeli.rethis.command.transaction.exec
import eu.vendeli.rethis.command.transaction.multi
import eu.vendeli.rethis.shared.types.CrossSlotOperationException
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.utils.CRC16
import eu.vendeli.rethis.shared.utils.unwrap
import eu.vendeli.rethis.types.common.ReadFrom
import eu.vendeli.rethis.types.interfaces.ReadFromStrategy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

class ClusterPipelineRoutingTest : TestCtx() {
    private val ranges = listOf(0..5460, 5461..10922, 10923..16383)

    private fun keyIn(range: IntRange) =
        generateSequence(0) { it + 1 }.map { "k$it" }.first { CRC16.slot(it.toByteArray()) in range }

    private val keys = ranges.map(::keyIn)

    private fun values(responses: List<RType>) = responses.map { it.unwrap<String>() }

    private fun withCluster(
        replicaOfLast: Boolean = false,
        readFrom: ReadFromStrategy? = null,
        block: suspend (ReThis, List<ScriptedClusterNode>, ScriptedClusterNode) -> Unit,
    ) {
        val nodes = List(3) { ScriptedClusterNode() }
        val replica = ScriptedClusterNode()
        val all = nodes + replica
        try {
            val reply = clusterSlotsReply(
                SlotBlock(ranges[0], nodes[0].address),
                SlotBlock(ranges[1], nodes[1].address),
                SlotBlock(ranges[2], nodes[2].address, if (replicaOfLast) listOf(replica.address) else emptyList()),
            )
            all.forEach { it.slotsReply = reply }
            replica.replicaOf = nodes[2].address
            val client = ReThis.cluster(listOf(nodes.first().address)) { readFrom?.let { readFromStrategy = it } }
            try {
                runBlocking { withTimeout(20.seconds) { block(client, nodes, replica) } }
            } finally {
                client.close()
            }
        } finally {
            all.forEach { it.close() }
        }
    }

    @Test
    fun `requests of different slots go to their owners and responses keep request order`() =
        withCluster { client, nodes, _ ->
            val responses = client.pipeline {
                get(keys[2])
                get(keys[0])
                get(keys[2])
                get(keys[1])
                get(keys[0])
            }

            values(responses) shouldBe listOf(keys[2], keys[0], keys[2], keys[1], keys[0]).map { "v:$it" }
            nodes.map { it.commandCount("GET") } shouldBe listOf(2, 1, 2)
            nodes[2].commandCount("GET", keys[2]) shouldBe 2
        }

    @Test
    fun `keyless requests in a pipeline keep their position and run on one node`() =
        withCluster { client, nodes, _ ->
            val responses = client.pipeline {
                get(keys[1])
                ping()
                get(keys[2])
                ping()
            }

            values(responses) shouldBe listOf("v:${keys[1]}", "PONG", "v:${keys[2]}", "PONG")
            nodes.sumOf { it.commandCount("PING") } shouldBe 2
            nodes.map { it.commandCount("PING") }.count { it > 0 } shouldBe 1
        }

    @Test
    fun `a slot group holding a write is sent to the master even when it starts with a read`() =
        withCluster(replicaOfLast = true, readFrom = ReadFrom.Replica) { client, nodes, replica ->
            client.pipeline {
                get(keys[2])
                set(keys[2], "x")
            }

            replica.commandCount("SET") shouldBe 0
            nodes[2].commandCount("SET") shouldBe 1
        }

    @Test
    fun `moved commands of a pipeline are retried once on the new owner`() = withCluster { client, nodes, _ ->
        nodes[0].override = { cmd ->
            if (cmd.first() ==
                "GET"
            ) "-MOVED ${CRC16.slot(cmd[1].toByteArray())} ${nodes[1].address.hostPort()}\r\n" else null
        }

        val responses = client.pipeline {
            get(keys[0])
            get(keys[1])
            get(keys[0])
        }

        values(responses) shouldBe listOf(keys[0], keys[1], keys[0]).map { "v:$it" }
        nodes.map { it.commandCount("GET") } shouldBe listOf(2, 3, 0)
    }

    @Test
    fun `a MULTI EXEC block of a pipeline stays together on the node of its slot`() =
        withCluster { client, nodes, _ ->
            val inTx = "{${keys[2]}}a"
            val inTx2 = "{${keys[2]}}b"

            val responses = client.pipeline {
                get(keys[0])
                multi()
                set(inTx, "1")
                set(inTx2, "2")
                exec()
                get(keys[1])
            }

            responses.size shouldBe 6
            nodes[2].commands.map { it.first() }.filter { it != "CLUSTER" } shouldBe
                listOf("MULTI", "SET", "SET", "EXEC")
            nodes[0].commands.filter { it.first() == "GET" }.size shouldBe 1
            nodes[1].commands.filter { it.first() == "GET" }.size shouldBe 1
            nodes.sumOf { it.commandCount("MULTI") } shouldBe 1
        }

    @Test
    fun `a MULTI EXEC block spanning slots fails before anything is sent`() = withCluster { client, nodes, _ ->
        shouldThrow<CrossSlotOperationException> {
            client.pipeline {
                get(keys[0])
                multi()
                set(keys[1], "1")
                set(keys[2], "2")
                exec()
            }
        }

        nodes.sumOf { it.commandCount("GET") + it.commandCount("MULTI") + it.commandCount("SET") } shouldBe 0
    }

    @Test
    fun `an unterminated MULTI block runs to the end of the pipeline on one node`() =
        withCluster { client, nodes, _ ->
            val inTx = "{${keys[2]}}a"

            client.pipeline {
                get(keys[0])
                multi()
                set(inTx, "1")
                get(inTx)
            }

            nodes[2].commands.map { it.first() }.filter { it != "CLUSTER" } shouldBe listOf("MULTI", "SET", "GET")
            nodes.sumOf { it.commandCount("MULTI") } shouldBe 1
        }

    @Test
    fun `a MOVED reply inside a MULTI block is not replayed outside the block`() = withCluster { client, nodes, _ ->
        val inTx = "{${keys[2]}}a"
        nodes[2].override = { cmd ->
            if (cmd.first() ==
                "SET"
            ) "-MOVED ${CRC16.slot(cmd[1].toByteArray())} ${nodes[1].address.hostPort()}\r\n" else null
        }

        client.pipeline {
            multi()
            set(inTx, "1")
            exec()
        }

        nodes[1].commandCount("SET") shouldBe 0
        nodes[2].commands.map { it.first() }.filter { it != "CLUSTER" } shouldBe listOf("MULTI", "SET", "EXEC")
    }
}
