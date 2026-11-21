package eu.vendeli.rethis.types

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.shared.types.ReThisException
import eu.vendeli.rethis.types.common.Address
import eu.vendeli.rethis.types.common.ClusterSnapshot
import eu.vendeli.rethis.types.common.RConnection
import eu.vendeli.rethis.types.common.ReadFrom
import eu.vendeli.rethis.types.common.SentinelSnapshot
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.io.Buffer

class ReadFromReplicaTest : TestCtx() {
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

    private fun providers(count: Int) = Array<ConnectionProvider>(count) { FakeProvider(Address("10.0.0.$it", 7000)) }

    private fun request(slot: Int? = null) = CommandRequest(Buffer(), RedisOperation.READ, command = "GET").let {
        if (slot != null) it.withSlot(slot) else it
    }

    private fun clusterSnapshot(replicas: IntArray): ClusterSnapshot {
        val owners = IntArray(16_384) { 0 }
        return ClusterSnapshot(owners, arrayOf(replicas, IntArray(0)), providers(2))
    }

    @Test
    fun `sentinel without replicas fails with a descriptive exception`() {
        val snap = SentinelSnapshot(0, providers(1))

        val ex = shouldThrow<ReThisException> { ReadFrom.Replica.pick(request(), snap) }

        ex.message shouldContain "replica"
    }

    @Test
    fun `cluster slot without replicas fails with a descriptive exception`() {
        val snap = clusterSnapshot(IntArray(0))

        val ex = shouldThrow<ReThisException> { ReadFrom.Replica.pick(request(slot = 0), snap) }

        ex.message shouldContain "replica"
    }

    @Test
    fun `sentinel picks the replica when one exists`() {
        val snap = SentinelSnapshot(0, providers(2))

        (ReadFrom.Replica.pick(request(), snap) === snap.providers[1]) shouldBe true
    }

    @Test
    fun `cluster picks the replica when one exists`() {
        val snap = clusterSnapshot(intArrayOf(1))

        (ReadFrom.Replica.pick(request(slot = 0), snap) === snap.providers[1]) shouldBe true
    }

    @Test
    fun `master preferred falls back to the master when no replica exists`() {
        val snap = SentinelSnapshot(0, providers(1))

        (ReadFrom.MasterPreferred.pick(request(), snap) === snap.providers[0]) shouldBe true
    }

    // Slot 9000 is owned by provider 1, which has provider 0 as its only replica.
    private fun offsetOwnerSnapshot(replicasOfOwner: IntArray) = ClusterSnapshot(
        IntArray(16_384) { 1 },
        arrayOf(IntArray(0), replicasOfOwner),
        providers(2),
    )

    @Test
    fun `replica lookup uses the owning master index rather than the slot number`() {
        val snap = offsetOwnerSnapshot(intArrayOf(0))

        (ReadFrom.Replica.pick(request(slot = 9000), snap) === snap.providers[0]) shouldBe true
    }

    @Test
    fun `replica preferred looks up replicas by the owning master index`() {
        val snap = offsetOwnerSnapshot(intArrayOf(0))

        (ReadFrom.ReplicaPreferred.pick(request(slot = 9000), snap) === snap.providers[0]) shouldBe true
    }

    @Test
    fun `replica reports missing replicas only when the owning master has none`() {
        val snap = offsetOwnerSnapshot(IntArray(0))

        shouldThrow<ReThisException> { ReadFrom.Replica.pick(request(slot = 9000), snap) }
        (ReadFrom.ReplicaPreferred.pick(request(slot = 9000), snap) === snap.providers[1]) shouldBe true
    }
}
