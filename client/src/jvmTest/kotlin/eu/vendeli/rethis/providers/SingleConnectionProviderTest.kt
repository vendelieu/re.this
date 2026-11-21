package eu.vendeli.rethis.providers

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.topology.FakeRespServer
import eu.vendeli.rethis.utils.isTransientFailure
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.network.sockets.InetSocketAddress
import kotlinx.coroutines.runBlocking
import kotlinx.io.IOException
import eu.vendeli.rethis.types.common.Address

class SingleConnectionProviderTest : TestCtx() {
    @Test
    fun `an unreachable node fails with a retryable transport error`() = runBlocking {
        val closedPort = FakeRespServer().use { (it.address.socket as InetSocketAddress).port }
        FakeRespServer().use { live ->
            val client = ReThis.standalone(live.address)
            try {
                val provider = SingleConnectionProvider(Address("127.0.0.1", closedPort), client)

                val failure = shouldThrow<IOException> { provider.borrowConnection() }

                failure.isTransientFailure() shouldBe true
            } finally {
                client.close()
            }
        }
    }
}
