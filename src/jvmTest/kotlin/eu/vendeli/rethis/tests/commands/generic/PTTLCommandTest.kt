package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.pTTL
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.types.options.SetExpire
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

class PTTLCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test PTTL command`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.pTTL("testKey") shouldBe -1L
    }

    @Test
    suspend fun `test TTL command with ttl positive`() {
        val time = Clock.System.now().plus(1.days)
        client.set("testKey", "testVal", SetExpire.EXAT(time)).shouldNotBeNull()
        client
            .pTTL("testKey")
            .shouldNotBeNull() shouldBeInRange
            (time.toEpochMilliseconds() - Clock.System.now().toEpochMilliseconds()).let {
                it.minus(1.seconds.inWholeMilliseconds).rangeTo(it + 1.seconds.inWholeMilliseconds)
            }
    }

    @Test
    suspend fun `test TTL command with non-existent key`() {
        client.pTTL("nonExistentKey") shouldBe -2L
    }
}
