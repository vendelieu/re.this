package eu.vendeli.rethis.core

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.command.list.blPop
import eu.vendeli.rethis.command.string.get
import eu.vendeli.rethis.configuration.StandaloneConfiguration
import eu.vendeli.rethis.shared.response.common.PopResult
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RedisOperation
import kotlinx.io.Buffer
import eu.vendeli.rethis.shared.types.CommandTimeoutException
import eu.vendeli.rethis.topology.FakeRespServer
import eu.vendeli.rethis.types.common.RespVer
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.network.sockets.*
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class CommandTimeoutDefaultTest : TestCtx() {
    private fun client(server: FakeRespServer, block: StandaloneConfiguration.() -> Unit = {}): ReThis {
        val socket = server.address.socket as InetSocketAddress
        return ReThis(socket.hostname, socket.port, RespVer.V2) {
            pool { setClientName = false }
            retry { times = 0 }
            block()
        }
    }

    @Test
    fun `command timeout is finite by default`() {
        StandaloneConfiguration(RespVer.V2).commandTimeout shouldBe 30.seconds
    }

    @Test
    fun `a null command timeout stays unbounded`() {
        val cfg = StandaloneConfiguration(RespVer.V2).apply { commandTimeout = null }

        cfg.commandTimeout shouldBe null
    }

    @Test
    fun `a stalled server fails a regular command with a command timeout`() {
        FakeRespServer(silent = true).use { server ->
            val client = client(server) { commandTimeout = 300.milliseconds }
            try {
                runBlocking { shouldThrow<CommandTimeoutException> { client.get("k") } }
            } finally {
                client.close()
            }
        }
    }

    @Test
    fun `a blocking command outlives the command timeout`() {
        FakeRespServer().use { server ->
            server.commandReplies["BLPOP"] = "*2\r\n\$1\r\nk\r\n\$1\r\nv\r\n"
            server.replyDelays["BLPOP"] = 900
            val client = client(server) { commandTimeout = 300.milliseconds }
            try {
                runBlocking { client.blPop("k", timeout = 1.0) } shouldBe PopResult("k", "v")
            } finally {
                client.close()
            }
        }
    }

    private fun request(blocking: Boolean) =
        CommandRequest(Buffer(), RedisOperation.READ, blocking, "X")

    @Test
    fun `a pipeline with a blocking command is exempt from the command timeout`() {
        val cfg = StandaloneConfiguration(RespVer.V2)

        cfg.commandTimeoutFor(listOf(request(false), request(true))) shouldBe null
    }

    @Test
    fun `a pipeline without blocking commands keeps the command timeout`() {
        val cfg = StandaloneConfiguration(RespVer.V2)

        cfg.commandTimeoutFor(listOf(request(false), request(false))) shouldBe 30.seconds
    }

    @Test
    fun `a null command timeout leaves every request unbounded`() {
        val cfg = StandaloneConfiguration(RespVer.V2).apply { commandTimeout = null }

        cfg.commandTimeoutFor(request(false)) shouldBe null
        cfg.commandTimeoutFor(listOf(request(false))) shouldBe null
    }

    @Test
    fun `a stalled server does not fail a command when the timeout is null`() {
        FakeRespServer().use { server ->
            server.commandReplies["GET"] = "\$1\r\nv\r\n"
            server.replyDelays["GET"] = 600
            val client = client(server) { commandTimeout = null }
            try {
                runBlocking { client.get("k") } shouldBe "v"
            } finally {
                client.close()
            }
        }
    }
}
