package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.shared.request.common.UpdateStrategyOption
import eu.vendeli.rethis.command.generic.pExpire
import eu.vendeli.rethis.command.string.set
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.seconds

class PEXPIRECommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test PEXPIRE command without options`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.pExpire("testKey", 10.seconds) shouldBe true
    }

    @Test
    suspend fun `test PEXPIRE command with EXPIRE option`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.pExpire("testKey", 10.seconds, UpdateStrategyOption.LT) shouldBe true
    }
}
