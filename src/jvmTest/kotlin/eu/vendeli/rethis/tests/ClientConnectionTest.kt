package eu.vendeli.rethis.tests

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.ping
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class ClientConnectionTest : ReThisTestCtx() {

    @Test
    fun `client disconnect test`(): Unit = runBlocking {
        client.ping()

        client.isDisconnected shouldBe false
        client.disconnect()
        client.isDisconnected shouldBe true
    }

    @Test
    fun `client reconnect test`(): Unit = runBlocking {
        client.ping()

        client.isDisconnected shouldBe false
        client.disconnect()
        client.isDisconnected shouldBe true

        client.reconnect()
        delay(100)
        client.isDisconnected shouldBe false
    }
}
