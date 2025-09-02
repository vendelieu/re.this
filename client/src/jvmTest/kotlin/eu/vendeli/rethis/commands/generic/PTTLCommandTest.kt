package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.generic.expireAt
import eu.vendeli.rethis.command.generic.pTtl
import eu.vendeli.rethis.command.string.set
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class PTTLCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test PTTL command`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.pTtl("testKey") shouldBe -1L
    }

    @Test
    suspend fun `test PTTL command with ttl positive`() {
        val time = Clock.System.now().plus(1.days)
        client.set("testKey", "testVal")
        client.expireAt("testKey", time)
        val ttl = client.pTtl("testKey").shouldNotBeNull()
        val leftTime = time.toEpochMilliseconds() - Clock.System.now().toEpochMilliseconds()

        ttl shouldBeInRange leftTime.let { it - 1000L..it + 1000L }
    }

    @Test
    suspend fun `test PTTL command with non-existent key`() {
        client.pTtl("nonExistentKey") shouldBe -2L
    }
}
