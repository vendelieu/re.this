package eu.vendeli.rethis.utils

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.types.common.RConnection
import io.kotest.matchers.shouldBe
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import kotlinx.io.readString
import kotlinx.io.writeString

class RConnectionBatchTest : TestCtx() {
    private fun batch(vararg commands: String): List<Buffer> = commands.map { Buffer().apply { writeString(it) } }

    @Test
    fun `batch can be sent twice with identical bytes and leaves the input intact`() = runBlocking {
        val selector = SelectorManager(Dispatchers.IO)
        val commands = arrayOf("*1\r\n\$4\r\nPING\r\n", "*2\r\n\$3\r\nGET\r\n\$1\r\nk\r\n")
        val expectedSize = commands.sumOf { it.length }
        val server = aSocket(selector).tcp().bind("127.0.0.1", 0)

        val received = async(Dispatchers.IO) {
            server.accept().use { conn ->
                val read = conn.openReadChannel()
                val write = conn.openWriteChannel(autoFlush = true)
                List(2) {
                    val bytes = read.readByteArray(expectedSize).decodeToString()
                    repeat(commands.size) { write.writeStringUtf8("+OK\r\n") }
                    bytes
                }
            }
        }

        val socket = aSocket(selector).tcp().connect(server.localAddress as InetSocketAddress)
        try {
            val conn = RConnection(socket, socket.openReadChannel(), socket.openWriteChannel(autoFlush = true))
            val payload = batch(*commands)

            repeat(2) {
                conn.doBatchRequest(payload)
                payload.map { it.size } shouldBe commands.map { it.length.toLong() }
            }

            val sent = received.await()
            sent[0] shouldBe commands.joinToString("")
            sent[1] shouldBe sent[0]
            payload.joinToString("") { it.copy().readString() } shouldBe commands.joinToString("")
        } finally {
            socket.close()
            server.close()
            selector.close()
        }
    }
}
