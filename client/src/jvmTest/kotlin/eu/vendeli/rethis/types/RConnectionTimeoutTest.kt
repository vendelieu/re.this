package eu.vendeli.rethis.types

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.configuration.StandaloneConfiguration
import eu.vendeli.rethis.core.ConnectionFactory
import eu.vendeli.rethis.shared.types.CommandTimeoutException
import eu.vendeli.rethis.topology.FakeRespServer
import eu.vendeli.rethis.types.common.RespVer
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.Buffer
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class RConnectionTimeoutTest : TestCtx() {
    private val ping = "*1\r\n\$4\r\nPING\r\n"

    private fun payload() = Buffer().apply { write(ping.encodeToByteArray()) }

    @Test
    fun `an enclosing timeout cancels the request instead of surfacing a command timeout`() = runBlocking {
        FakeRespServer(silent = true).use { server ->
            val conn = ConnectionFactory(StandaloneConfiguration(RespVer.V2), Job())
                .createConnOrNull(server.address.socket)!!

            val result = withTimeoutOrNull(200.milliseconds) {
                conn.doRequest(payload(), commandTimeout = 30.seconds)
            }

            result shouldBe null
        }
    }

    @Test
    fun `the command timeout still surfaces as a command timeout`() = runBlocking {
        FakeRespServer(silent = true).use { server ->
            val conn = ConnectionFactory(StandaloneConfiguration(RespVer.V2), Job())
                .createConnOrNull(server.address.socket)!!

            shouldThrow<CommandTimeoutException> { conn.doRequest(payload(), commandTimeout = 200.milliseconds) }
        }
    }
}
