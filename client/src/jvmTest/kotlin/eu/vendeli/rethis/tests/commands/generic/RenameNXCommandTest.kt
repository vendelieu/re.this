package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.del
import eu.vendeli.rethis.commands.renameNx
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class RenameNXCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test RENAMENX command`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.del("newKey1")
        client.renameNx("testKey", "newKey1") shouldBe true
    }

    @Test
    suspend fun `test RENAMENX command with existing key`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.set("newKey", "newVal").shouldNotBeNull()
        client.renameNx("testKey", "newKey") shouldBe false
    }
}
