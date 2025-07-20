package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.api.spec.common.request.string.SetExpire
import eu.vendeli.rethis.command.generic.pTtl
import eu.vendeli.rethis.command.string.set
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

class PTTLCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test PTTL command`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.pTtl("testKey") shouldBe -1L
    }

    @Test
    suspend fun `test TTL command with ttl positive`() {
        val time = Clock.System.now().plus(1.days)
        client.set("testKey", "testVal", SetExpire.ExAt(time)).shouldNotBeNull()
        client
            .pTtl("testKey")
            .shouldNotBeNull() shouldBeInRange
            (time.toEpochMilliseconds() - Clock.System.now().toEpochMilliseconds()).let {
                it.minus(1.seconds.inWholeMilliseconds).rangeTo(it + 1.seconds.inWholeMilliseconds)
            }
    }

    @Test
    suspend fun `test TTL command with non-existent key`() {
        client.pTtl("nonExistentKey") shouldBe -2L
    }
}
