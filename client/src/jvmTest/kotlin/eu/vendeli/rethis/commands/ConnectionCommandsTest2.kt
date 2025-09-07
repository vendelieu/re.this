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
    fun `test AUTH with wrong credentials throws`() = runTest {
        shouldThrow<ReThisException> {
            client.auth(username = "bad-user", password = "bad-pass".toCharArray())
        }.message.shouldNotBeNull()
    }

    @Test
    fun `test CLIENT GETNAME when not set can be null`() = runTest {
        // First ensure no name is set by setting to empty string
        client.clientSetName("").shouldBeTrue()

        // Depending on server/setup it can be null or some default; accept null or non-blank
        client.clientGetName()?.ifBlank { null }.shouldBeNull()
    }

    @Test
    fun `test CLIENT ID returns positive id`() = runTest {
        val id = client.clientId()
        id shouldBeGreaterThan 0
    }

    @Test
    fun `test CLIENT INFO returns non-empty info`() = runTest {
        val info = client.clientInfo()
        info.shouldNotBeBlank()
        info shouldContain "id="
    }

    @Test
    fun `test CLIENT LIST returns non-empty list`() = runTest {
        val list = client.clientList()
        list.shouldNotBeBlank()
        list shouldContain "id="
    }

    @Test
    fun `test CLIENT PAUSE with small timeout`() = runTest {
        // Use a very small timeout to avoid disrupting suite
        client.clientPause(timeout = 1L, mode = ClientPauseMode.ALL).shouldBeTrue()
    }

    @Test
    fun `test CLIENT REPLY ON-OFF-ON`() = runTest {
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
    fun `test CLIENT TRACKING ON and OFF`() = runTest {
        // Turn ON without special modes, then OFF
        client.clientTracking(ClientStandby.ON, ClientTrackingMode.NOLOOP).shouldBeTrue()
        client.clientTracking(ClientStandby.OFF).shouldBeTrue()
    }

    @Test
    fun `test CLIENT UNBLOCK current id is likely not blocked and returns false`() = runTest {
        val id = client.clientId()
        // Expect false because this connection isn't blocked
        client.clientUnblock(id, ClientUnblockType.TIMEOUT).shouldBeFalse()
    }

    @Test
    fun `test QUIT on a separate client`() = runTest {
        // Use a temporary client so we don't close the shared test client
        val temp = createClient()
        temp.quit().shouldBeTrue()
    }
}
