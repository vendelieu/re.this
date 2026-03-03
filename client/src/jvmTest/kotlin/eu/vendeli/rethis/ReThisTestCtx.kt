package eu.vendeli.rethis

import com.redis.testcontainers.RedisContainer
import eu.vendeli.rethis.codecs.connection.PingCommandCodec
import eu.vendeli.rethis.command.server.flushAll
import eu.vendeli.rethis.configuration.StandaloneConfiguration
import eu.vendeli.rethis.shared.request.common.FlushType
import eu.vendeli.rethis.types.common.RespVer
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.test.config.DefaultTestConfig
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import org.testcontainers.utility.DockerImageName
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

private val TEST_TIMEOUT = 3.minutes

abstract class TestCtx : AnnotationSpec() {
    protected val timestamp: Instant get() = Clock.System.now()
    protected val defaultCharset = Charsets.UTF_8

    @Suppress("TestFunctionName")
    protected suspend fun Buffer(block: suspend Buffer.() -> Unit): Buffer {
        val buff = Buffer()
        buff.block()

        return buff
    }
}

abstract class ReThisTestCtx : TestCtx() {
    init {
        defaultTestConfig = DefaultTestConfig(timeout = TEST_TIMEOUT)
    }

    protected val targetDb = 1L
    protected suspend fun connectionProvider() = client.topology.route(
        PingCommandCodec.encode(Charsets.UTF_8, null),
    )

    protected fun createClient(
        host: String = redis.host,
        port: Int = redis.firstMappedPort,
        protocol: RespVer = RespVer.V3,
        cfg: StandaloneConfiguration.() -> Unit = {},
    ): ReThis = ReThis(host, port, protocol) {
        DEFAULT_SIMPLE_TEST_CFG()
        cfg()
    }

    @AfterAll
    fun cleanup() = runBlocking {
        client.flushAll(FlushType.SYNC)
    }

    companion object {
        protected val DEFAULT_TEST_CFG: StandaloneConfiguration.() -> Unit = {
            socket {
                timeout = TEST_TIMEOUT.inWholeMilliseconds
            }
            retry {
                times = 1
            }
        }

        @JvmStatic
        protected val DEFAULT_SIMPLE_TEST_CFG: StandaloneConfiguration.() -> Unit = {
            DEFAULT_TEST_CFG()
            pool {
                minIdleConnections = 1
            }
        }

        @JvmStatic
        protected val redis = RedisContainer(DockerImageName.parse("redis:8.2")).also {
            it.start()
        }

        @JvmStatic
        protected val client = ReThis(redis.host, redis.firstMappedPort, RespVer.V3, DEFAULT_TEST_CFG)
    }
}

@Suppress("UNCHECKED_CAST")
internal inline fun <T> Any?.safeCast() = this as? T

@Suppress("UNCHECKED_CAST")
internal inline fun <T> Any?.cast() = this as T
