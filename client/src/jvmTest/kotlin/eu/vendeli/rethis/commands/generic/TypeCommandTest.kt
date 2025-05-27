package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.commands.type
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class TypeCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test TYPE command`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.type("testKey") shouldBe "string"
    }

    @Test
    suspend fun `test TYPE command with non-existent key`() {
        client.type("nonExistentKey") shouldBe "none"
    }
}
