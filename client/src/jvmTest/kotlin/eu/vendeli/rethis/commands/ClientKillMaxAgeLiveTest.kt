package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.TestEnv.TARGET_REDIS_VER
import eu.vendeli.rethis.command.connection.clientId
import eu.vendeli.rethis.command.connection.clientKill
import eu.vendeli.rethis.shared.request.connection.ClientKillOptions
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import org.opentest4j.TestAbortedException
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

/** `CLIENT KILL MAXAGE` appeared in Redis 7.4; the test image must be at least that version. */
class ClientKillMaxAgeLiveTest : ReThisTestCtx() {
    private val maxAgeSupported = TARGET_REDIS_VER.split(".").map { it.toIntOrNull() ?: 0 }.let { parts ->
        val major = parts.first()
        val minor = parts.getOrElse(1) { 0 }
        major > 7 || (major == 7 && minor >= 4)
    }

    @Test
    suspend fun `CLIENT KILL MAXAGE closes only connections older than the given age`() {
        if (!maxAgeSupported) throw TestAbortedException(
            "CLIENT KILL MAXAGE needs Redis 7.4+, image is $TARGET_REDIS_VER",
        )
        val victim = createClient()
        val victimId = victim.clientId()
        delay(2.seconds)

        // a connection of this age is younger than one hour: nothing may be killed
        client.clientKill(ClientKillOptions.Id(victimId), ClientKillOptions.MaxAge(1.hours)) shouldBe 0L
        // and older than one second: the victim connection must be killed
        client.clientKill(ClientKillOptions.Id(victimId), ClientKillOptions.MaxAge(1.seconds)) shouldBe 1L
        // the connection is gone, so killing it by id again finds nothing
        client.clientKill(ClientKillOptions.Id(victimId)) shouldBe 0L
    }
}
