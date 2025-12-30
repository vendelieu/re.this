package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.connection.*
import eu.vendeli.rethis.shared.request.connection.*
import eu.vendeli.rethis.shared.types.ReThisException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeBlank
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

class ConnectionCommandsTest2 : ReThisTestCtx() {
    @Test
    suspend fun `test AUTH with wrong credentials throws`() {
        shouldThrow<ReThisException> {
            client.auth(username = "bad-user", password = "bad-pass".toCharArray())
        }.message.shouldNotBeNull()
    }

    @Test
    suspend fun `test CLIENT GETNAME when not set can be null`() {
        // First ensure no name is set by setting to empty string
        client.clientSetName("").shouldBeTrue()

        // Depending on server/setup it can be null or some default; accept null or non-blank
        client.clientGetName()?.ifBlank { null }.shouldBeNull()
    }

    @Test
    suspend fun `test CLIENT ID returns positive id`() {
        val id = client.clientId()
        id shouldBeGreaterThan 0
    }

    @Test
    suspend fun `test CLIENT INFO returns non-empty info`() {
        val info = client.clientInfo()
        info.shouldNotBeBlank()
        info shouldContain "id="
    }

    @Test
    suspend fun `test CLIENT LIST returns non-empty list`() {
        val list = client.clientList()
        list.shouldNotBeBlank()
        list shouldContain "id="
    }

    @Test
    suspend fun `test CLIENT PAUSE with small timeout`() {
        // Use a very small timeout to avoid disrupting suite
        client.clientPause(timeout = 1L, mode = ClientPauseMode.ALL).shouldBeTrue()
    }

    @Test
    suspend fun `test CLIENT REPLY ON-OFF-ON`() {
        // Toggle OFF then back ON, finally RESET to be conservative
        withTimeoutOrNull(200.milliseconds) {
            client.clientReply(ClientReplyMode.OFF).shouldBeTrue()
        }.shouldBeNull()
        client.clientReply(ClientReplyMode.ON).shouldBeTrue()
        withTimeoutOrNull(200.milliseconds) {
            client.clientReply(ClientReplyMode.SKIP).shouldBeTrue()
        }.shouldBeNull()
    }

    @Test
    suspend fun `test CLIENT TRACKING ON and OFF`() {
        // Turn ON without special modes, then OFF
        client.clientTracking(ClientStandby.ON, ClientTrackingMode.NOLOOP).shouldBeTrue()
        client.clientTracking(ClientStandby.OFF).shouldBeTrue()
    }

    @Test
    suspend fun `test CLIENT UNBLOCK current id is likely not blocked and returns false`() {
        val id = client.clientId()
        // Expect false because this connection isn't blocked
        client.clientUnblock(id, ClientUnblockType.TIMEOUT).shouldBeFalse()
    }

    @Test
    suspend fun `test QUIT on a separate client`() {
        // Use a temporary client so we don't close the shared test client
        val temp = createClient()
        temp.quit().shouldBeTrue()
    }
}
