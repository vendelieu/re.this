package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.expire
import eu.vendeli.rethis.commands.expireTime
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class ExpireTimeCommandTest : ReThisTestCtx() {
    @Test
    fun `test EXPIRETIME command`(): Unit = runBlocking {
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
    fun `test EXPIRETIME command with non-existent key`(): Unit = runBlocking {
        client.expireTime("nonExistentKey") shouldBe -2L
    }
}
