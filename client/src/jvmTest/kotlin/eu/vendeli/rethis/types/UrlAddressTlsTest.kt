package eu.vendeli.rethis.types

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.configuration.StandaloneConfiguration
import eu.vendeli.rethis.core.withServerName
import eu.vendeli.rethis.types.common.RespVer
import eu.vendeli.rethis.types.common.UrlAddress
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.network.tls.*
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.UnixSocketAddress

class UrlAddressTlsTest : TestCtx() {
    @Test
    fun `rediss scheme parses like redis with credentials port and db`() {
        val addr = UrlAddress("rediss://alice:s3cret@cache.local:6380/4")

        addr.useTls shouldBe true
        addr.address.socket
            .let { it as InetSocketAddress }
            .hostname shouldBe "cache.local"
        addr.address.socket
            .let { it as InetSocketAddress }
            .port shouldBe 6380
        addr.username shouldBe "alice"
        addr.password shouldBe "s3cret"
        addr.db shouldBe 4
    }

    @Test
    fun `redis scheme does not enable tls`() {
        UrlAddress("redis://cache.local").useTls shouldBe false
    }

    @Test
    fun `rediss without port uses the default port`() {
        (UrlAddress("rediss://cache.local").address.socket as InetSocketAddress).port shouldBe 6379
    }

    @Test
    fun `tls scheme installs the default tls config`() {
        val cfg = StandaloneConfiguration(RespVer.V2)

        cfg.applyUrlTls(true)

        cfg.tls shouldNotBe null
    }

    @Test
    fun `plain scheme leaves tls disabled`() {
        val cfg = StandaloneConfiguration(RespVer.V2)

        cfg.applyUrlTls(false)

        cfg.tls shouldBe null
    }

    @Test
    fun `explicit tls block wins over the url default`() {
        val cfg = StandaloneConfiguration(RespVer.V2)
        val explicit = TLSConfigBuilder().apply { serverName = "explicit.host" }.build()
        cfg.tls { explicit }

        cfg.applyUrlTls(true)

        cfg.tls shouldBe explicit
    }

    @Test
    fun `an unset server name defaults to the connected host`() {
        val config = TLSConfigBuilder().build().withServerName(InetSocketAddress("node-a.example", 6380))

        config.serverName shouldBe "node-a.example"
    }

    @Test
    fun `the server name follows each node address`() {
        val base = TLSConfigBuilder().build()

        base.withServerName(InetSocketAddress("node-a", 1)).serverName shouldBe "node-a"
        base.withServerName(InetSocketAddress("node-b", 2)).serverName shouldBe "node-b"
        base.serverName shouldBe null
    }

    @Test
    fun `an explicit server name wins over the connected host`() {
        val explicit = TLSConfigBuilder().apply { serverName = "explicit.host" }.build()

        explicit.withServerName(InetSocketAddress("node-a", 1)).serverName shouldBe "explicit.host"
    }

    @Test
    fun `a non inet address leaves the config unchanged`() {
        val base = TLSConfigBuilder().build()

        base.withServerName(UnixSocketAddress("/tmp/redis.sock")).serverName shouldBe null
    }

    @Test
    fun `rediss with an explicit tls block resolves to the explicit config`() {
        val explicit = TLSConfigBuilder().apply { serverName = "explicit.host" }.build()
        val cfg = StandaloneConfiguration(RespVer.V2).apply { tls { explicit } }
        val addr = UrlAddress("rediss://cache.local")

        cfg.applyUrlTls(addr.useTls)

        cfg.tls?.serverName shouldBe "explicit.host"
    }
}
