package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.commands.ttl
import eu.vendeli.rethis.types.options.SetExpire
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Clock
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
        client.set("testKey", "testVal", SetExpire.EXAT(time)).shouldNotBeNull()
        client
            .ttl("testKey")
            .shouldNotBeNull() shouldBeInRange (time.epochSeconds - Clock.System.now().epochSeconds).let {
            it.minus(1).rangeTo(it + 1)
        }
    }

    @Test
    suspend fun `test TTL command with non-existent key`() {
        client.ttl("nonExistentKey") shouldBe -2L
    }
}
