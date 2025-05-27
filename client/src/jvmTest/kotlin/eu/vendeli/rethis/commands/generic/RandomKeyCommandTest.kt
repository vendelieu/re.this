package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.commands.randomKey
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull

class RandomKeyCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test RANDOMKEY command`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.randomKey().shouldNotBeNull()
    }
}
