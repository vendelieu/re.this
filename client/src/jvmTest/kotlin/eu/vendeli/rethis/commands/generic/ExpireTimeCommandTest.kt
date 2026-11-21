package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.generic.expire
import eu.vendeli.rethis.command.generic.expireTime
import eu.vendeli.rethis.command.string.set
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.seconds

class ExpireTimeCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test EXPIRETIME command`() {
        client.set("testKey", "testVal")

        val expectedExpireTime = timestamp.epochSeconds + 10L
        client.expire("testKey", 10.seconds)

        client.expireTime("testKey").shouldNotBeNull() shouldBeInRange
            (expectedExpireTime - 1L)..(expectedExpireTime + 2L)
    }

    @Test
    suspend fun `test EXPIRETIME command with non-existent key`() {
        client.expireTime("nonExistentKey") shouldBe -2L
    }
}
