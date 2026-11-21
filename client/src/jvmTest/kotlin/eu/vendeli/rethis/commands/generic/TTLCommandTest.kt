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

private const val TTL_TOLERANCE_SECONDS = 5L

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

        ttl shouldBeInRange (leftTime - TTL_TOLERANCE_SECONDS)..(leftTime + TTL_TOLERANCE_SECONDS)
    }

    @Test
    suspend fun `test TTL command with non-existent key`() {
        client.ttl("nonExistentKey") shouldBe -2L
    }
}
