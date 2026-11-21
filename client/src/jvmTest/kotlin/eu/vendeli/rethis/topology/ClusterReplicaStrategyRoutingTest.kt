package eu.vendeli.rethis.topology

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.command.string.get
import eu.vendeli.rethis.shared.utils.CRC16
import eu.vendeli.rethis.types.common.ReadFrom
import eu.vendeli.rethis.types.interfaces.ReadFromStrategy
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

/** `AnyReplica` and `LowestLatency` must stay inside the shard that owns the slot of the key. */
class ClusterReplicaStrategyRoutingTest : TestCtx() {
    private val ranges = listOf(0..5460, 5461..10922, 10923..16383)

    private fun keyIn(range: IntRange) =
        generateSequence(0) { it + 1 }.map { "k$it" }.first { CRC16.slot(it.toByteArray()) in range }

    private val keys = ranges.map(::keyIn)
    private val reads = 12

    private class Topology(
        val masters: List<ScriptedClusterNode>,
        val firstReplica: ScriptedClusterNode,
        val thirdReplica: ScriptedClusterNode,
    )

    // shards one and three have a replica each, shard two has none
    private fun withStrategy(strategy: ReadFromStrategy, block: suspend (ReThis, Topology) -> Unit) {
        val masters = List(3) { ScriptedClusterNode() }
        val topology = Topology(masters, ScriptedClusterNode(), ScriptedClusterNode())
        val all = masters + topology.firstReplica + topology.thirdReplica
        try {
            val reply = clusterSlotsReply(
                SlotBlock(ranges[0], masters[0].address, listOf(topology.firstReplica.address)),
                SlotBlock(ranges[1], masters[1].address),
                SlotBlock(ranges[2], masters[2].address, listOf(topology.thirdReplica.address)),
            )
            all.forEach { it.slotsReply = reply }
            topology.firstReplica.replicaOf = masters[0].address
            topology.thirdReplica.replicaOf = masters[2].address
            val client = ReThis.cluster(listOf(masters.first().address)) { readFromStrategy = strategy }
            try {
                runBlocking { withTimeout(20.seconds) { block(client, topology) } }
            } finally {
                client.close()
            }
        } finally {
            all.forEach { it.close() }
        }
    }

    private fun assertReadsStayInShard(strategy: ReadFromStrategy) = withStrategy(strategy) { client, topology ->
        repeat(reads) { client.get(keys[2]) shouldBe "v:${keys[2]}" }
        topology.thirdReplica.commandCount("GET") shouldBe reads
        topology.firstReplica.commandCount("GET") shouldBe 0
        topology.masters.sumOf { it.commandCount("GET") } shouldBe 0

        repeat(reads) { client.get(keys[0]) shouldBe "v:${keys[0]}" }
        topology.firstReplica.commandCount("GET") shouldBe reads
        topology.thirdReplica.commandCount("GET") shouldBe reads
    }

    private fun assertFallsBackToMaster(strategy: ReadFromStrategy) = withStrategy(strategy) { client, topology ->
        repeat(reads) { client.get(keys[1]) shouldBe "v:${keys[1]}" }
        topology.masters[1].commandCount("GET") shouldBe reads
        topology.firstReplica.commandCount("GET") shouldBe 0
        topology.thirdReplica.commandCount("GET") shouldBe 0
    }

    @Test
    fun `any replica reads from a replica of the slot shard`() = assertReadsStayInShard(ReadFrom.AnyReplica)

    @Test
    fun `any replica falls back to the master of a shard without replicas`() =
        assertFallsBackToMaster(ReadFrom.AnyReplica)

    @Test
    fun `lowest latency reads from a replica of the slot shard`() = assertReadsStayInShard(ReadFrom.LowestLatency)

    @Test
    fun `lowest latency falls back to the master of a shard without replicas`() =
        assertFallsBackToMaster(ReadFrom.LowestLatency)
}
