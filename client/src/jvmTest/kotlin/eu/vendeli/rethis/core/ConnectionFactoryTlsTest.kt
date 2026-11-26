package eu.vendeli.rethis.core

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.annotations.ReThisInternal
import eu.vendeli.rethis.configuration.StandaloneConfiguration
import eu.vendeli.rethis.types.common.RespVer
import io.kotest.matchers.shouldBe
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.tls.TLSConfigBuilder
import kotlinx.coroutines.*
import kotlinx.io.Buffer
import kotlinx.io.readString
import java.io.BufferedReader
import java.io.EOFException
import java.net.InetAddress
import java.net.SocketException
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLException
import javax.net.ssl.SSLServerSocket
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import kotlin.coroutines.CoroutineContext

@OptIn(ReThisInternal::class)
class ConnectionFactoryTlsTest : TestCtx() {
    @Test
    suspend fun `disposing a TLS connection closes its writer and socket`() =
        withTlsPeer(RespVer.V2) { factory, server, writer ->
            val peer = async(Dispatchers.IO) {
                server.acceptPeer().use { socket ->
                    val input = socket.inputStream.bufferedReader(Charsets.US_ASCII)
                    List(3) { input.readLine() } shouldBe listOf("*1", "\$4", "PING")
                    socket.outputStream.write("+PONG\r\n".encodeToByteArray())
                    socket.outputStream.flush()
                    input.expectPeerClose()
                }
            }

            val connection = checkNotNull(factory.createConnOrNull(server.address()))
            try {
                val ping = Buffer().apply { write("*1\r\n\$4\r\nPING\r\n".encodeToByteArray()) }
                connection.doRequest(ping).readString() shouldBe "+PONG\r\n"
            } finally {
                factory.dispose(connection)
            }

            connection.output.isClosedForWrite shouldBe true
            writer.await().join()
            connection.socket.socketContext.join()
            peer.await()
            factory.isReachedLimit() shouldBe false
        }

    @Test
    suspend fun `cancelling TLS connection preparation closes its writer and socket`() =
        withTlsPeer(RespVer.V3) { factory, server, writer ->
            val helloReceived = CompletableDeferred<Unit>()
            val peer = async(Dispatchers.IO) {
                server.acceptPeer().use { socket ->
                    val input = socket.inputStream.bufferedReader(Charsets.US_ASCII)
                    List(5) { input.readLine() } shouldBe listOf("*2", "\$5", "HELLO", "\$1", "3")
                    helloReceived.complete(Unit)
                    input.expectPeerClose()
                }
            }

            val connection = async { factory.createConnOrNull(server.address()) }
            helloReceived.await()
            factory.isReachedLimit() shouldBe true
            connection.cancelAndJoin()

            peer.await()
            writer.await().join()
            factory.isReachedLimit() shouldBe false
        }

    private suspend fun withTlsPeer(
        protocol: RespVer,
        block: suspend CoroutineScope.(ConnectionFactory, SSLServerSocket, Deferred<Job>) -> Unit,
    ) = withTimeout(HANG_GUARD_MILLIS.toLong()) {
        val password = "rethis-test".toCharArray()
        val keys = KeyStore.getInstance("PKCS12").apply {
            // This public test fixture contains a key used only by the loopback peer.
            checkNotNull(ConnectionFactoryTlsTest::class.java.getResourceAsStream("/localhost-test.p12")).use {
                load(it, password)
            }
        }
        val keyManagers = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
            init(keys, password)
        }
        val trusted = KeyStore.getInstance("PKCS12").apply {
            load(null, null)
            setCertificateEntry("localhost", keys.getCertificate("localhost"))
        }
        val trustManagers = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
            init(trusted)
        }
        val ssl = SSLContext.getInstance("TLSv1.2").apply { init(keyManagers.keyManagers, null, null) }
        val server = ssl.serverSocketFactory
            .createServerSocket(0, 1, InetAddress.getByName("127.0.0.1")) as SSLServerSocket
        server.use {
            server.soTimeout = HANG_GUARD_MILLIS
            val root = SupervisorJob()
            try {
                val dispatcher = TlsWriterDispatcher()
                val configuration = StandaloneConfiguration(protocol).apply {
                    maxConnections = 1
                    generalDispatcher = dispatcher
                    tls {
                        TLSConfigBuilder()
                            .apply {
                                serverName = "localhost"
                                trustManager = trustManagers.trustManagers.filterIsInstance<X509TrustManager>().single()
                            }.build()
                    }
                }
                block(ConnectionFactory(configuration, root), server, dispatcher.writer)
            } finally {
                root.cancel()
            }
        }
    }

    private fun SSLServerSocket.acceptPeer() = (accept() as SSLSocket).apply {
        enabledProtocols = arrayOf("TLSv1.2")
        soTimeout = HANG_GUARD_MILLIS
    }

    private fun SSLServerSocket.address() = InetSocketAddress(inetAddress.hostAddress, localPort)

    private fun BufferedReader.expectPeerClose() {
        val line = try {
            readLine()
        } catch (_: SocketException) {
            null
        } catch (e: SSLException) {
            if (e.cause !is EOFException && e.cause !is SocketException) throw e
            null
        }
        line shouldBe null
    }

    private class TlsWriterDispatcher : CoroutineDispatcher() {
        val writer = CompletableDeferred<Job>()

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            // Observe the application-data writer separately from the TLS encoder and TCP writer.
            if (context[CoroutineName]?.name == "cio-tls-output-loop") writer.complete(context.job)
            Dispatchers.Default.dispatch(context, block)
        }
    }

    private companion object {
        const val HANG_GUARD_MILLIS = 10_000
    }
}
