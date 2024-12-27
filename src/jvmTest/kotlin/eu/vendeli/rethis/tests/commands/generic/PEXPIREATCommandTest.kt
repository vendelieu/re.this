package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.pExpireAt
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.types.options.UpdateStrategyOption
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class PEXPIREATCommandTest : ReThisTestCtx() {
    @Test
    fun `test PEXPIREAT command without options`(): Unit = runTest {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.pExpireAt("testKey", 1643723400000L) shouldBe true
    }

    @Test
    fun `test PEXPIREAT command with EXPIRE option`(): Unit = runTest {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.pExpireAt("testKey", 1643723400000L, UpdateStrategyOption.LT) shouldBe true
    }
}
