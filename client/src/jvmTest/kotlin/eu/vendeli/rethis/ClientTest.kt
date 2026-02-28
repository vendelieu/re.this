package eu.vendeli.rethis

import eu.vendeli.rethis.command.connection.ping
import io.kotest.core.spec.IsolationMode
import io.kotest.matchers.shouldBe

class ClientTest : ReThisTestCtx() {
    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf

    @Test
    suspend fun `client disconnect test`() {
        val testClient = createClient()
        testClient.ping()
        testClient.isActive shouldBe true
        testClient.close()
        testClient.isActive shouldBe false
    }
}
