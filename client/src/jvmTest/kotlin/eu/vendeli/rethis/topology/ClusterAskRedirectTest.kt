package eu.vendeli.rethis.topology

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.shared.types.ClusterException
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedirectAskException
import eu.vendeli.rethis.types.common.Address
import eu.vendeli.rethis.types.common.ClusterSnapshot
import eu.vendeli.rethis.types.common.RConnection
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import kotlinx.io.readString
import kotlinx.io.writeString

class ClusterAskRedirectTest : TestCtx() {
    private class FakeProvider(
        override val node: Address,
    ) : ConnectionProvider() {
        var closed = false
        override suspend fun execute(request: CommandRequest): Buffer = Buffer()
        override fun close() {
            closed = true
        }

        override suspend fun borrowConnection(): RConnection = throw NotImplementedError()
        override suspend fun releaseConnection(conn: RConnection) {}
        override fun disposeConnection(conn: RConnection) {}
        override fun hasSpareConnection(): Boolean = false
    }

    private val nodeA = FakeProvider(Address("10.0.0.1", 7000))
    private val nodeB = FakeProvider(Address("10.0.0.2", 7001))
    private val slot = 42

    private fun snapshot() = ClusterSnapshot(
        slotOwner = IntArray(16_384),
        replicaIndices = arrayOf(IntArray(0), IntArray(0)),
        providers = arrayOf(nodeA, nodeB),
    )

    private fun ask(host: String, port: Int) = RedirectAskException("ASK $slot $host:$port", slot, host, port)

    @Test
    fun `targets the node named in the ask error and never the slot owner`() = runBlocking {
        val snap = snapshot()
        var used: ConnectionProvider? = null

        withAskTarget(snap, ask("10.0.0.2", 7001), { error("must reuse snapshot provider") }) { used = it }

        (used === nodeB) shouldBe true
        nodeA.closed shouldBe false
        nodeB.closed shouldBe false
    }

    @Test
    fun `creates and closes a temporary provider for an unknown address leaving snapshot unchanged`() = runBlocking {
        val snap = snapshot()
        val owners = snap.slotOwner.copyOf()
        val providers = snap.providers.copyOf()
        val temp = FakeProvider(Address("10.0.0.3", 7002))
        var used: ConnectionProvider? = null
        var created: Address? = null

        withAskTarget(snap, ask("10.0.0.3", 7002), {
            created = it
            temp
        }) { used = it }

        created shouldBe temp.node
        (used === temp) shouldBe true
        temp.closed shouldBe true
        snap.slotOwner.contentEquals(owners) shouldBe true
        snap.providers.toList().shouldContainExactly(providers.toList())
    }

    @Test
    fun `closes the temporary provider when the call fails`() = runBlocking {
        val temp = FakeProvider(Address("10.0.0.3", 7002))
        runCatching {
            withAskTarget(snapshot(), ask("10.0.0.3", 7002), { temp }) { error("boom") }
        }
        temp.closed shouldBe true
    }

    @Test
    fun `returns the command reply with the asking reply consumed`() = runBlocking {
        val replies = ArrayDeque(listOf("+OK\r\n", "$3\r\nfoo\r\n"))
        val sent = mutableListOf<String>()
        val asking = Buffer().apply { writeString("ASKING") }
        val command = Buffer().apply { writeString("GET k") }

        val result = requestAfterAsking(asking, command) {
            sent += it.copy().readString()
            Buffer().apply { writeString(replies.removeFirst()) }
        }

        sent.shouldContainExactly("ASKING", "GET k")
        result.readString() shouldBe "$3\r\nfoo\r\n"
    }

    @Test
    fun `fails when the asking reply is an error`() = runBlocking {
        val ex = runCatching {
            requestAfterAsking(Buffer(), Buffer()) { Buffer().apply { writeString("-ERR nope\r\n") } }
        }.exceptionOrNull()
        (ex is ClusterException) shouldBe true
    }
}
