package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.generic.waitAof
import eu.vendeli.rethis.command.string.set
import eu.vendeli.rethis.shared.types.ReThisException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull

class WaitAofCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test WAITAOF command`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        shouldThrow<ReThisException> {
            client.waitAof(
                1L,
                1L,
                1000L,
            )
        }
    }
}
