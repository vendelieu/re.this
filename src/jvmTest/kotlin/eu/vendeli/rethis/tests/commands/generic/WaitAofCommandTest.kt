package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.ReThisException
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.commands.waitAof
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class WaitAofCommandTest : ReThisTestCtx() {
    @Test
    fun `test WAITAOF command`(): Unit = runTest {
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
