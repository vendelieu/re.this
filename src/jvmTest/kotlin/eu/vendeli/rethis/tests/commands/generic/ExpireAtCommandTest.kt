package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.expireAt
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.types.options.UpdateStrategyOption
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.seconds

class ExpireAtCommandTest : ReThisTestCtx() {
    @Test
    fun `test EXPIREAT command without options`(): Unit = runBlocking {
        client.set("testKey", "testVal")

        val unixStamp = Clock.System.now().plus(10.seconds)

        client.expireAt("testKey", unixStamp) shouldBe true
    }

    @Test
    fun `test EXPIREAT command with EXPIRE option`(): Unit = runBlocking {
        client.set("testKey", "testVal")

        val unixStamp = Clock.System.now().plus(10.seconds)

        client.expireAt("testKey", unixStamp, UpdateStrategyOption.LT) shouldBe true
    }
}
