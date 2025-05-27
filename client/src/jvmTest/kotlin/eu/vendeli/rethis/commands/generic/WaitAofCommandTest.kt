package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.api.spec.common.types.ReThisException
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.commands.waitAof
import eu.vendeli.rethis.ReThisTestCtx
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
