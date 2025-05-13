package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.commands.unlink
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class UnlinkCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test UNLINK command with single key`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.unlink("testKey") shouldBe 1L
    }

    @Test
    suspend fun `test UNLINK command with multiple keys`() {
        client.set("testKey1", "testVal1").shouldNotBeNull()
        client.set("testKey2", "testVal2").shouldNotBeNull()
        client.unlink("testKey1", "testKey2") shouldBe 2L
    }
}
