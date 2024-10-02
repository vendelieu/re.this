package eu.vendeli.rethis

import com.redis.testcontainers.RedisContainer
import io.kotest.core.spec.style.AnnotationSpec
import io.ktor.network.sockets.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

abstract class ReThisTestCtx(
    withJsonModule: Boolean = false,
) : AnnotationSpec() {
    protected val targetDb = 1L
    protected val timestamp: Instant get() = Clock.System.now()

    private val redis = RedisContainer(if (!withJsonModule) "redis:7.4.0" else "redislabs/rejson").apply {
        start()
    }
    private val connAddr = InetSocketAddress(redis.redisHost, redis.redisPort)

    protected val client = ReThis(connAddr)
}
