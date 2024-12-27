package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.commands.ttl
import eu.vendeli.rethis.types.options.SetExpire
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days

class TTLCommandTest : ReThisTestCtx() {
    @Test
    fun `test TTL command`(): Unit = runBlocking {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.ttl("testKey") shouldBe -1L
    }

    @Test
    fun `test TTL command with ttl positive`(): Unit = runBlocking {
        val time = Clock.System.now().plus(1.days)
        client.set("testKey", "testVal", SetExpire.EXAT(time)).shouldNotBeNull()
        client
            .ttl("testKey")
            .shouldNotBeNull() shouldBeInRange (time.epochSeconds - Clock.System.now().epochSeconds).let {
            it.minus(1).rangeTo(it + 1)
        }
    }

    @Test
    fun `test TTL command with non-existent key`(): Unit = runBlocking {
        client.ttl("nonExistentKey") shouldBe -2L
    }
}
