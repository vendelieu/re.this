package eu.vendeli.rethis.core

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.configuration.StandaloneConfiguration
import eu.vendeli.rethis.topology.FakeRespServer
import eu.vendeli.rethis.types.common.RespVer
import eu.vendeli.rethis.utils.CLIENT_NAME
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking

class ConnectionFactoryTest : TestCtx() {
    private fun connectAndRecord(cfg: StandaloneConfiguration): List<List<String>> =
        FakeRespServer().use { server ->
            val rootJob = Job()
            try {
                runBlocking {
                    val factory = ConnectionFactory(cfg, rootJob)
                    val conn = factory.createConnOrNull(server.address.socket)
                    conn?.let { factory.dispose(it) }
                }
                server.commands
            } finally {
                rootJob.cancel()
            }
        }

    @Test
    fun `RESP2 with setClientName and no auth or db names the connection`() {
        val cfg = StandaloneConfiguration(RespVer.V2).apply { pool { setClientName = true } }

        val commands = connectAndRecord(cfg)

        commands.size shouldBe 1
        commands.single().map { it.uppercase() } shouldBe listOf("HELLO", "2", "SETNAME", CLIENT_NAME.uppercase())
    }

    @Test
    fun `RESP2 without setClientName, auth or db sends nothing`() {
        val cfg = StandaloneConfiguration(RespVer.V2).apply { pool { setClientName = false } }

        connectAndRecord(cfg) shouldBe emptyList()
    }

    @Test
    fun `negative db is rejected`() {
        val cfg = StandaloneConfiguration(RespVer.V2).apply { db = -1 }

        val ex = shouldThrow<IllegalArgumentException> { ConnectionFactory(cfg, Job()) }

        ex.message shouldContain "db"
        ex.message shouldContain "-1"
    }

    private fun connectTwiceAndRecord(cfg: StandaloneConfiguration, server: FakeRespServer) {
        val rootJob = Job()
        try {
            runBlocking {
                val factory = ConnectionFactory(cfg, rootJob)
                repeat(2) { factory.createConnOrNull(server.address.socket)?.let(factory::dispose) }
            }
        } finally {
            rootJob.cancel()
        }
    }

    @Test
    fun `RESP2 falls back to CLIENT SETNAME when HELLO is unknown and remembers it`() {
        val cfg = StandaloneConfiguration(RespVer.V2).apply { pool { setClientName = true } }

        FakeRespServer(helloReply = UNKNOWN_HELLO).use { server ->
            connectTwiceAndRecord(cfg, server)

            server.commands.map { it.map(String::uppercase) } shouldBe listOf(
                listOf("HELLO", "2", "SETNAME", CLIENT_NAME.uppercase()),
                listOf("CLIENT", "SETNAME", CLIENT_NAME.uppercase()),
                listOf("CLIENT", "SETNAME", CLIENT_NAME.uppercase()),
            )
            server.commandCount("HELLO") shouldBe 1
        }
    }

    @Test
    fun `RESP2 fallback sends AUTH with the password only before CLIENT SETNAME`() {
        val cfg = StandaloneConfiguration(RespVer.V2).apply {
            auth("secret".toCharArray())
            pool { setClientName = true }
        }

        FakeRespServer(helloReply = UNKNOWN_HELLO).use { server ->
            connectTwiceAndRecord(cfg, server)

            val afterHello = server.commands.drop(1).take(2)
            afterHello.map { it.first().uppercase() } shouldBe listOf("AUTH", "CLIENT")
            afterHello.first() shouldBe listOf("AUTH", "secret")
            server.commandCount("HELLO") shouldBe 1
        }
    }

    @Test
    fun `RESP3 fails with a hint when HELLO is unknown`() {
        val cfg = StandaloneConfiguration(RespVer.V3)

        FakeRespServer(helloReply = UNKNOWN_HELLO).use { server ->
            val rootJob = Job()
            try {
                val ex = shouldThrow<Exception> {
                    runBlocking { ConnectionFactory(cfg, rootJob).createConnOrNull(server.address.socket) }
                }

                ex.message shouldContain "RESP3 requires Redis 6 or newer"
            } finally {
                rootJob.cancel()
            }
        }
    }

    private companion object {
        const val UNKNOWN_HELLO = "-ERR unknown command 'HELLO', with args beginning with: '2' \r\n"
    }
}
