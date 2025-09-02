package eu.vendeli.rethis

import eu.vendeli.rethis.command.connection.ping
import io.kotest.core.spec.IsolationMode
import io.kotest.matchers.shouldBe

class ClientTest : ReThisTestCtx() {
    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf

    @Test
    suspend fun `client disconnect test`() {
        client.ping()
        client.isActive shouldBe true
        client.close()
        client.isActive shouldBe false
    }
}
