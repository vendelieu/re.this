package eu.vendeli.rethis.topology

import eu.vendeli.rethis.TestCtx
import io.kotest.matchers.shouldBe
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CopyOnWriteArrayList

class FakeServerShutdownTest : TestCtx() {
    private fun captureUncaught(block: () -> Unit): List<Throwable> {
        val uncaught = CopyOnWriteArrayList<Throwable>()
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { _, e -> uncaught += e }
        try {
            block()
            runBlocking { delay(300) }
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(previous)
        }
        return uncaught.toList()
    }

    @Test
    fun `closing fake servers repeatedly never leaks an uncaught exception`() {
        val uncaught = captureUncaught {
            repeat(200) {
                FakeRespServer().close()
                ScriptedClusterNode().close()
            }
            runBlocking {
                SelectorManager(Dispatchers.IO).use { selector ->
                    repeat(50) {
                        val server = FakeRespServer()
                        val socket = aSocket(selector).tcp().connect(server.address.socket)
                        socket.openWriteChannel(autoFlush = true).writeStringUtf8("*1\r\n\$4\r\nPING\r\n")
                        server.close()
                        socket.close()
                    }
                }
            }
        }

        uncaught shouldBe emptyList()
    }
}
