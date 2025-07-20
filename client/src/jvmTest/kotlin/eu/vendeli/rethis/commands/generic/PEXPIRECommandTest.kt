package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.api.spec.common.request.common.UpdateStrategyOption
import eu.vendeli.rethis.command.generic.pExpire
import eu.vendeli.rethis.command.string.set
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class PEXPIRECommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test PEXPIRE command without options`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.pExpire("testKey", 10000L) shouldBe true
    }

    @Test
    suspend fun `test PEXPIRE command with EXPIRE option`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.pExpire("testKey", 10000L, UpdateStrategyOption.LT) shouldBe true
    }
}
