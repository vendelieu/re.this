package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.shared.request.common.UpdateStrategyOption
import eu.vendeli.rethis.command.generic.pExpireAt
import eu.vendeli.rethis.command.string.set
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.time.Instant

class PEXPIREATCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test PEXPIREAT command without options`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.pExpireAt("testKey", Instant.DISTANT_FUTURE) shouldBe true
    }

    @Test
    suspend fun `test PEXPIREAT command with EXPIRE option`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.pExpireAt("testKey", Instant.DISTANT_FUTURE, UpdateStrategyOption.LT) shouldBe true
    }
}
