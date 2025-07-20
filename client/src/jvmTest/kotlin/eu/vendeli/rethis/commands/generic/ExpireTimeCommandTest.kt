package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.generic.expire
import eu.vendeli.rethis.command.generic.expireTime
import eu.vendeli.rethis.command.string.set
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class ExpireTimeCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test EXPIRETIME command`() {
        client.set("testKey", "testVal")

        val expireTime = timestamp.epochSeconds + 10L
        client.expire("testKey", 10)

        client.expireTime("testKey").shouldNotBeNull().minus(timestamp.epochSeconds) shouldBeInRange expireTime
            .minus(
                timestamp.epochSeconds,
            ).let {
                it.minus(1).rangeTo(it.plus(1))
            }
    }

    @Test
    suspend fun `test EXPIRETIME command with non-existent key`() {
        client.expireTime("nonExistentKey") shouldBe -2L
    }
}
