package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.connection.clientPause
import eu.vendeli.rethis.command.generic.pTtl
import eu.vendeli.rethis.command.generic.ttl
import eu.vendeli.rethis.command.generic.wait
import eu.vendeli.rethis.command.list.blPop
import eu.vendeli.rethis.command.stream.xRead
import eu.vendeli.rethis.command.string.set
import eu.vendeli.rethis.shared.request.string.SetExpire
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.longs.shouldBeInRange
import io.kotest.matchers.nulls.shouldBeNull
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/** Observes the unit of the raw-number time arguments against a real server: a wrong unit would block for minutes. */
class TimeUnitLiveTest : ReThisTestCtx() {
    @Test
    suspend fun `SET EX and PX are applied in seconds and milliseconds`() {
        client.set("timeUnitEx", "v", SetExpire.Ex(90.seconds))
        client.ttl("timeUnitEx") shouldBeInRange 88L..90L
        client.set("timeUnitPx", "v", SetExpire.Px(90_000.milliseconds))
        client.pTtl("timeUnitPx") shouldBeInRange 88_000L..90_000L
    }

    @Test
    suspend fun `BLPOP timeout is in seconds and accepts fractions`() {
        val started = TimeSource.Monotonic.markNow()
        client.blPop("timeUnitEmptyList", timeout = 0.3)
        started.elapsedNow() shouldBeLessThan 5.seconds
    }

    @Test
    suspend fun `XREAD BLOCK is in milliseconds`() {
        val started = TimeSource.Monotonic.markNow()
        client.xRead(listOf("timeUnitEmptyStream"), listOf("$"), milliseconds = 300L).shouldBeNull()
        started.elapsedNow() shouldBeLessThan 5.seconds
    }

    @Test
    suspend fun `WAIT timeout is in milliseconds`() {
        val started = TimeSource.Monotonic.markNow()
        client.wait(1L, 300L)
        started.elapsedNow() shouldBeLessThan 5.seconds
    }

    @Test
    suspend fun `CLIENT PAUSE timeout is in milliseconds`() {
        client.clientPause(200L)
        delay(1.seconds)
        val started = TimeSource.Monotonic.markNow()
        client.ttl("timeUnitEx")
        started.elapsedNow() shouldBeLessThan 5.seconds
    }
}
