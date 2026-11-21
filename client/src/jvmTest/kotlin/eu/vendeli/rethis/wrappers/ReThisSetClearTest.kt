package eu.vendeli.rethis.wrappers

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.topology.FakeRespServer
import eu.vendeli.rethis.types.common.RespVer
import io.kotest.matchers.shouldBe
import io.ktor.network.sockets.*

class ReThisSetClearTest : TestCtx() {
    @Test
    fun `clear deletes the key with a single DEL`() {
        FakeRespServer().use { server ->
            server.commandReplies["DEL"] = ":1\r\n"
            val socket = server.address.socket as InetSocketAddress
            val client = ReThis(socket.hostname, socket.port, RespVer.V2) { pool { setClientName = false } }
            try {
                client.Set("someSet").clear()
            } finally {
                client.close()
            }

            server.commands shouldBe listOf(listOf("DEL", "someSet"))
        }
    }
}
