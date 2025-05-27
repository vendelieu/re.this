package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.commands.move
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class MoveCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test MOVE command`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.move("testKey", targetDb) shouldBe true
    }
}
