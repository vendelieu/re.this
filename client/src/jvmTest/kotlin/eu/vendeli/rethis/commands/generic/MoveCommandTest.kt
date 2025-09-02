package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.generic.move
import eu.vendeli.rethis.command.string.set
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class MoveCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test MOVE command`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.move("testKey", targetDb) shouldBe true
    }
}
