package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.api.spec.common.request.string.SetExpire
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
        client.set("testKey", "testVal", SetExpire.ExAt(time)).shouldNotBeNull()
        client
            .ttl("testKey")
            .shouldNotBeNull() shouldBeInRange
            time.epochSeconds.let {
                it.minus(1).rangeTo(it + 1)
            }
    }

    @Test
    suspend fun `test TTL command with non-existent key`() {
        client.ttl("nonExistentKey") shouldBe -2L
    }
}
