package eu.vendeli.rethis.securedinst

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.command.connection.ping
import io.kotest.core.spec.IsolationMode
import io.kotest.matchers.shouldBe
import io.ktor.network.tls.TLSConfigBuilder
import java.io.File
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class SecuredInstTest : TestCtx() {
    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf

    @Test
    @Ignore
    suspend fun `client disconnect test`() {
        val rootPath = File("").absolutePath
        val pathPrefix = "src/jvmTest/kotlin/${javaClass.packageName.replace('.', '/')}/"
        val trustManager = loadTrustManagerFromCA(File(rootPath, "${pathPrefix}certs/ca.crt"))

        val tlsConfig = TLSConfigBuilder()
            .apply {
                this.trustManager = trustManager
            }.build()

        val client = ReThis {
            auth("yourStrongRedisPassword".toCharArray())
            tls { tlsConfig }
        }

        client.ping() shouldBe "PONG"
    }

    private fun loadTrustManagerFromCA(caFile: File): X509TrustManager {
        val cf = CertificateFactory.getInstance("X.509")
        val caCert = caFile.inputStream().use { cf.generateCertificate(it) }

        // Create a new empty key store
        val ks = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null, null)
            setCertificateEntry("redis-ca", caCert)
        }

        // Create TrustManager
        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(ks)

        return tmf.trustManagers.filterIsInstance<X509TrustManager>().first()
    }
}
