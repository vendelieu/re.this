package eu.vendeli.rethis.core

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.command.string.set
import eu.vendeli.rethis.shared.types.ReThisException
import eu.vendeli.rethis.topology.FakeRespServer
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds

class PooledConnectionReuseTest : TestCtx() {
    private fun singleConnectionClient(server: FakeRespServer) = ReThis.standalone(server.address) {
        maxConnections = 1
        pool { minIdleConnections = 0 }
        retry {
            times = 1
            initialDelay = 1.milliseconds
        }
    }

    @Test
    fun `a server error reply keeps the pooled connection for the next command`() = runBlocking {
        FakeRespServer(replies = mapOf("SET" to "-BUSY Redis is busy running a script\r\n")).use { server ->
            val client = singleConnectionClient(server)
            try {
                shouldThrow<ReThisException> { client.set("k", "v") }
                server.replies = emptyMap()

                client.set("k", "v")

                server.acceptedConnections shouldBe 1
            } finally {
                client.close()
            }
        }
    }

    @Test
    fun `a socket closed mid-command disposes the connection and the next command opens a new one`() = runBlocking {
        FakeRespServer(dropOn = setOf("SET")).use { server ->
            val client = singleConnectionClient(server)
            try {
                runCatching { client.set("k", "v") }.isFailure shouldBe true
                server.dropOn = emptySet()

                client.set("k", "v")

                server.acceptedConnections shouldBe 2
            } finally {
                client.close()
            }
        }
    }
}
