package eu.vendeli.rethis.topology

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.codecs.string.GetCommandCodec
import eu.vendeli.rethis.codecs.transaction.UnwatchCommandCodec
import eu.vendeli.rethis.codecs.transaction.WatchCommandCodec
import eu.vendeli.rethis.types.coroutine.CoLocalConn
import eu.vendeli.rethis.types.interfaces.LoggerFactory
import io.kotest.matchers.shouldBe
import io.ktor.util.logging.*
import kotlinx.coroutines.withContext
import java.lang.reflect.Proxy
import java.util.concurrent.CopyOnWriteArrayList

class ConnectionStateCommandsTest : ReThisTestCtx() {
    private val warnings = CopyOnWriteArrayList<String>()

    private val recordingFactory = LoggerFactory {
        Proxy.newProxyInstance(Logger::class.java.classLoader, arrayOf(Logger::class.java)) { _, method, args ->
            if (method.name == "warn" && !args.isNullOrEmpty() && args[0] is String) warnings += args[0] as String
            when (method.returnType) {
                Boolean::class.javaPrimitiveType -> false
                String::class.java -> "recording"
                else -> null
            }
        } as Logger
    }

    @Test
    suspend fun `connection state helper flags only connection scoped commands`() {
        WatchCommandCodec.encode(Charsets.UTF_8, "k").isConnectionStateful() shouldBe true
        UnwatchCommandCodec.encode(Charsets.UTF_8).isConnectionStateful() shouldBe true
        GetCommandCodec.encode(Charsets.UTF_8, "k").isConnectionStateful() shouldBe false
    }

    @BeforeEach
    fun resetWarnings() = warnings.clear()

    @Test
    suspend fun `watch through the pool logs one warning`() {
        val isolated = createClient { loggerFactory = recordingFactory }
        try {
            isolated.topology.handle(WatchCommandCodec.encode(Charsets.UTF_8, "connStateKey"))
            isolated.topology.handle(UnwatchCommandCodec.encode(Charsets.UTF_8))
        } finally {
            isolated.close()
        }

        warnings.count { it.startsWith("WATCH ") } shouldBe 1
    }

    @Test
    suspend fun `watch on a pinned connection logs no warning`() {
        val isolated = createClient { loggerFactory = recordingFactory }
        try {
            val provider = isolated.topology.route(GetCommandCodec.encode(Charsets.UTF_8, "k"))
            val conn = provider.borrowConnection()
            try {
                withContext(CoLocalConn(conn, isTx = false)) {
                    isolated.topology.handle(WatchCommandCodec.encode(Charsets.UTF_8, "connStateKey"))
                    isolated.topology.handle(UnwatchCommandCodec.encode(Charsets.UTF_8))
                }
            } finally {
                provider.releaseConnection(conn)
            }
        } finally {
            isolated.close()
        }

        warnings.none { it.contains("connection-scoped") } shouldBe true
    }

    @Test
    suspend fun `plain commands through the pool log no warning`() {
        val isolated = createClient { loggerFactory = recordingFactory }
        try {
            isolated.topology.handle(GetCommandCodec.encode(Charsets.UTF_8, "connStateKey"))
        } finally {
            isolated.close()
        }

        warnings.none { it.contains("connection-scoped") } shouldBe true
    }
}
