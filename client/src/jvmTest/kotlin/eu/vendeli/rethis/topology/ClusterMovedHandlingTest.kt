package eu.vendeli.rethis.topology

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedirectMovedException
import eu.vendeli.rethis.shared.types.RedisOperation
import eu.vendeli.rethis.types.common.Address
import eu.vendeli.rethis.types.common.ClusterSnapshot
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.ktor.network.sockets.InetSocketAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.io.Buffer
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ClusterMovedHandlingTest : TestCtx() {
    private val movedSlot = 5

    private fun moved(slot: Int, target: Address) = (target.socket as InetSocketAddress).let {
        RedirectMovedException("MOVED $slot ${it.hostname}:${it.port}", slot, it.hostname, it.port)
    }

    private fun write(slot: Int) = CommandRequest(Buffer(), RedisOperation.WRITE, command = "SET").withSlot(slot)

    private fun withCluster(
        block:
            suspend (
                a: ScriptedClusterNode,
                b: ScriptedClusterNode,
                c: ScriptedClusterNode,
                m: ClusterTopologyManager,
            ) -> Unit,
    ) {
        ScriptedClusterNode().use { a ->
            ScriptedClusterNode().use { b ->
                ScriptedClusterNode().use { c ->
                    val reply = clusterSlotsReply(SlotBlock(0..8191, a.address), SlotBlock(8192..16383, b.address))
                    a.slotsReply = reply
                    b.slotsReply = reply
                    val client = ReThis.cluster(listOf(a.address)) {
                        movedBackoffPeriod = 1.milliseconds
                    }
                    try {
                        runBlocking {
                            val manager = client.topology as ClusterTopologyManager
                            withTimeout(10.seconds) { while (manager.currentSnapshot() == null) delay(10.milliseconds) }
                            block(a, b, c, manager)
                        }
                    } finally {
                        client.close()
                    }
                }
            }
        }
    }

    private fun ClusterSnapshot.ownerOf(slot: Int) = providers[slotOwner[slot]].node

    @Test
    fun `a redirect to a known node publishes a new snapshot and leaves the captured one untouched`() = withCluster {
        a,
        b,
        _,
        manager,
        ->
        val before = manager.currentSnapshot()!!
        before.ownerOf(movedSlot) shouldBe a.address

        shouldThrow<RedirectMovedException> { manager.handleFailure(write(movedSlot), moved(movedSlot, b.address)) }

        before.ownerOf(movedSlot) shouldBe a.address
        manager.currentSnapshot()!!.ownerOf(movedSlot) shouldBe b.address
        manager.route(write(movedSlot)).node shouldBe b.address
    }

    @Test
    fun `a redirect for a slot outside the slot range is rethrown and the snapshot is unchanged`() = withCluster {
        _,
        b,
        _,
        manager,
        ->
        val before = manager.currentSnapshot()!!
        val redirect = moved(99_999, b.address)

        val thrown = shouldThrow<RedirectMovedException> { manager.handleFailure(write(movedSlot), redirect) }

        thrown shouldBeSameInstanceAs redirect
        manager.currentSnapshot() shouldBeSameInstanceAs before
    }

    @Test
    fun `a redirect to an unknown node adds it without touching the previous snapshot`() = withCluster {
        a,
        _,
        c,
        manager,
        ->
        val before = manager.currentSnapshot()!!
        val providersBefore = before.providers.size

        shouldThrow<RedirectMovedException> { manager.handleFailure(write(movedSlot), moved(movedSlot, c.address)) }

        before.providers.size shouldBe providersBefore
        before.ownerOf(movedSlot) shouldBe a.address
        val after = manager.currentSnapshot()!!
        after.providers.size shouldBe providersBefore + 1
        after.ownerOf(movedSlot) shouldBe c.address
    }

    @Test
    fun `concurrent redirects to known and unknown nodes are all kept and the unknown node is added once`() =
        withCluster { _, b, c, manager ->
            val before = manager.currentSnapshot()!!.providers.size
            val slots = 0 until 40

            kotlinx.coroutines.coroutineScope {
                slots
                    .map { slot ->
                        async(Dispatchers.Default) {
                            val target = if (slot % 2 == 0) c.address else b.address
                            runCatching { manager.handleFailure(write(slot), moved(slot, target)) }
                        }
                    }.awaitAll()
            }

            val after = manager.currentSnapshot()!!
            after.providers.size shouldBe before + 1
            after.providers.count { it.node == c.address } shouldBe 1
            slots.forEach { slot -> after.ownerOf(slot) shouldBe if (slot % 2 == 0) c.address else b.address }
        }
}
