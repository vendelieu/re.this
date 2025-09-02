package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.shared.request.common.UpdateStrategyOption
import eu.vendeli.rethis.command.generic.expireAt
import eu.vendeli.rethis.command.string.set
import io.kotest.matchers.shouldBe
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class ExpireAtCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test EXPIREAT command without options`() {
        client.set("testKey", "testVal")

        val unixStamp = Clock.System.now().plus(10.seconds)

        client.expireAt("testKey", unixStamp) shouldBe true
    }

    @Test
    suspend fun `test EXPIREAT command with EXPIRE option`() {
        client.set("testKey", "testVal")

        val unixStamp = Clock.System.now().plus(10.seconds)

        client.expireAt("testKey", unixStamp, UpdateStrategyOption.LT) shouldBe true
    }
}
