package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.pExpireAt
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.types.options.UpdateStrategyOption
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class PEXPIREATCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test PEXPIREAT command without options`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.pExpireAt("testKey", 1643723400000L) shouldBe true
    }

    @Test
    suspend fun `test PEXPIREAT command with EXPIRE option`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.pExpireAt("testKey", 1643723400000L, UpdateStrategyOption.LT) shouldBe true
    }
}
