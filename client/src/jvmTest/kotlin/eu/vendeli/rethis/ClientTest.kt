package eu.vendeli.rethis

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.ping
import io.kotest.core.spec.IsolationMode
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay

class ClientTest : ReThisTestCtx() {
    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf

    @Test
    suspend fun `client disconnect test`() {
        client.ping()

        client.isDisconnected shouldBe false
        client.disconnect()
        client.isDisconnected shouldBe true
    }

    @Test
    suspend fun `client reconnect test`() {
        client.reconnect()
        client.ping()

        client.isDisconnected shouldBe false
        client.disconnect()
        client.isDisconnected shouldBe true

        client.reconnect()
        delay(100)
        client.isDisconnected shouldBe false
    }

    @Test
    suspend fun `client shutdown test`() {
        client.shutdown()
        client.rootJob.isCancelled shouldBe true
    }
}
