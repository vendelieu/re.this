package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.expireAt
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.types.options.UpdateStrategyOption
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Clock
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
