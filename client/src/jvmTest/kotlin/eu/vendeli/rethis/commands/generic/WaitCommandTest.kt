package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.generic.wait
import eu.vendeli.rethis.command.string.set
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class WaitCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test WAIT command`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.wait(1L, 1000L) shouldBe 0L
    }
}
