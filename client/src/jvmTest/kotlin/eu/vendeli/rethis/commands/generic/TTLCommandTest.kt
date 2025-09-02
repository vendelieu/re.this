package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.generic.expireAt
import eu.vendeli.rethis.command.generic.ttl
import eu.vendeli.rethis.command.string.set
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class TTLCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test TTL command`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.ttl("testKey") shouldBe -1L
    }

    @Test
    suspend fun `test TTL command with ttl positive`() {
        val time = Clock.System.now().plus(1.days)
        client.set("testKey", "testVal")
        client.expireAt("testKey", time)
        val ttl = client.ttl("testKey").shouldNotBeNull()
        val leftTime = time.epochSeconds - Clock.System.now().epochSeconds

        ttl shouldBeInRange leftTime.let { it - 1000L..it + 1000L }
    }

    @Test
    suspend fun `test TTL command with non-existent key`() {
        client.ttl("nonExistentKey") shouldBe -2L
    }
}
