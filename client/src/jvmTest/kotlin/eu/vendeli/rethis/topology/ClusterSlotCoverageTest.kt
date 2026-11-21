package eu.vendeli.rethis.topology

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.shared.request.cluster.SlotRange
import eu.vendeli.rethis.shared.response.cluster.ClusterNode
import eu.vendeli.rethis.shared.response.common.HostAndPort
import eu.vendeli.rethis.shared.types.ClusterException
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.types.common.Address
import eu.vendeli.rethis.types.common.ClusterSnapshot
import eu.vendeli.rethis.types.common.RConnection
import eu.vendeli.rethis.types.common.ReadFrom
import eu.vendeli.rethis.types.common.masterIndexOf
import eu.vendeli.rethis.types.interfaces.ReadFromStrategy
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.io.Buffer

class ClusterSlotCoverageTest : TestCtx() {
    private class FakeProvider(
        override val node: Address,
    ) : ConnectionProvider() {
        override suspend fun execute(request: CommandRequest): Buffer = Buffer()
        override fun close() {}
        override suspend fun borrowConnection(): RConnection = throw NotImplementedError()
        override suspend fun releaseConnection(conn: RConnection) {}
        override fun disposeConnection(conn: RConnection) {}
        override fun hasSpareConnection(): Boolean = false
    }

    private val uncoveredSlot = 9000
    private val master = HostAndPort("10.0.0.1", 7000)

    private fun providers(count: Int) = Array<ConnectionProvider>(count) { FakeProvider(Address("10.0.0.$it", 7000)) }

    private fun snapshot(
        replicas: Array<IntArray> = arrayOf(IntArray(0), IntArray(0)),
        masterIdx: Int = 1,
    ) = ClusterSnapshot(
        slotOwner = buildSlotOwner(
            listOf(ClusterNode(master, listOf(SlotRange(0, 8191)), emptyList())),
            mapOf(master to masterIdx),
        ),
        replicaIndices = replicas,
        providers = providers(replicas.size),
    )

    private fun request(op: RedisOperation = RedisOperation.READ, slot: Int = uncoveredSlot) =
        CommandRequest(Buffer(), op, command = "GET").withSlot(slot)

    @Test
    fun `slots outside every reported range are marked uncovered`() {
        val owners = snapshot().slotOwner

        owners.size shouldBe 16_384
        (0..8191).all { owners[it] == 1 } shouldBe true
        (8192..16_383).all { owners[it] == -1 } shouldBe true
    }

    @Test
    fun `master lookup of an uncovered slot fails with a cluster exception`() {
        val ex = shouldThrow<ClusterException> { snapshot().masterIndexOf(uncoveredSlot) }

        ex.message shouldContain "No master for slot $uncoveredSlot"
    }

    @Test
    fun `master lookup of a covered slot returns the owner index`() {
        snapshot().masterIndexOf(100) shouldBe 1
    }

    @Test
    fun `every read strategy fails fast for an uncovered slot`() {
        val strategies: List<ReadFromStrategy> = listOf(
            ReadFrom.Master,
            ReadFrom.MasterPreferred,
            ReadFrom.Replica,
            ReadFrom.ReplicaPreferred,
            ReadFrom.AnyReplica,
        )

        strategies.forEach {
            shouldThrow<ClusterException> { it.pick(request(), snapshot(arrayOf(IntArray(0)), masterIdx = 0)) }
        }
    }

    @Test
    fun `any replica never treats the uncovered marker as a master index`() {
        val snap = snapshot(arrayOf(IntArray(0), IntArray(0), IntArray(0)))

        // a keyless request may be served by any non-master node
        val keyless = CommandRequest(Buffer(), RedisOperation.READ, command = "PING")
        val picked = ReadFrom.AnyReplica.pick(keyless, snap)

        (picked === snap.providers[0] || picked === snap.providers[2]) shouldBe true
    }

    @Test
    fun `any replica of a covered slot without replicas falls back to its master`() {
        val snap = snapshot(arrayOf(IntArray(0), IntArray(0), IntArray(0)))

        ReadFrom.AnyReplica.pick(request(slot = 100), snap) shouldBe snap.providers[1]
    }
}
