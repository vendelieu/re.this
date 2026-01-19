package eu.vendeli.rethis

import com.redis.testcontainers.RedisContainer
import eu.vendeli.rethis.codecs.connection.PingCommandCodec
import eu.vendeli.rethis.configuration.ReThisConfiguration
import eu.vendeli.rethis.configuration.StandaloneConfiguration
import io.kotest.core.spec.style.AnnotationSpec
import kotlinx.io.Buffer
import org.testcontainers.utility.DockerImageName
import kotlin.time.Clock
import kotlin.time.Instant

abstract class TestCtx : AnnotationSpec() {
    protected val timestamp: Instant get() = Clock.System.now()
    protected val defaultCharset = Charsets.UTF_8

    @Suppress("UNCHECKED_CAST")
    protected inline fun <T> Any?.safeCast() = this as? T

    @Suppress("TestFunctionName")
    protected suspend fun Buffer(block: suspend Buffer.() -> Unit): Buffer {
        val buff = Buffer()
        buff.block()

        return buff
    }
}

abstract class ReThisTestCtx(
    withJsonModule: Boolean = false,
) : TestCtx() {
    protected val targetDb = 1L

    protected val redis = RedisContainer(
        DockerImageName.parse(if (!withJsonModule) "redis:7.4.0" else "redislabs/rejson"),
    ).apply { start() }

    private var reThis: ReThis = ReThis(redis.host, redis.firstMappedPort)
    protected val client get() = reThis

    protected suspend fun connectionProvider() = client.topology.route(
        PingCommandCodec.encode(Charsets.UTF_8, null),
    )

    protected fun rewriteCfg(block: ReThisConfiguration.() -> Unit) {
        client.cfg.block()
    }

    protected fun resetClient(new: ReThis) {
        reThis = new
    }

    protected fun createClient(
        cfg: StandaloneConfiguration.() -> Unit = {},
    ): ReThis = ReThis(redis.host, redis.firstMappedPort, configurator = cfg)
}
