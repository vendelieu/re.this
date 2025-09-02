package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.generic.del
import eu.vendeli.rethis.command.string.set
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class DelCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test DEL command with single key`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.del("testKey") shouldBe 1L
    }

    @Test
    suspend fun `test DEL command with multiple keys`() {
        client.set("testKey1", "testVal1").shouldNotBeNull()
        client.set("testKey2", "testVal2").shouldNotBeNull()
        client.del("testKey1", "testKey2") shouldBe 2L
    }
}
