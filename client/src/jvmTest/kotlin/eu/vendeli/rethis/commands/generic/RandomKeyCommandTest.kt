package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.generic.randomKey
import eu.vendeli.rethis.command.string.set
import io.kotest.matchers.nulls.shouldNotBeNull

class RandomKeyCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test RANDOMKEY command`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.randomKey().shouldNotBeNull()
    }
}
