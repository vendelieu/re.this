package eu.vendeli.rethis

import com.redis.testcontainers.RedisContainer
import eu.vendeli.rethis.codecs.connection.PingCommandCodec
import io.kotest.core.spec.style.AnnotationSpec
import org.testcontainers.utility.DockerImageName
import kotlin.time.Clock
import kotlin.time.Instant

abstract class ReThisTestCtx(
    withJsonModule: Boolean = false,
) : AnnotationSpec() {
    protected val targetDb = 1L
    protected val timestamp: Instant get() = Clock.System.now()

    protected val redis = RedisContainer(
        DockerImageName.parse(if (!withJsonModule) "redis:${RedisContainer.DEFAULT_TAG}" else "redislabs/rejson"),
    ).apply { start() }

    private var rethis: ReThis = ReThis(redis.host, redis.firstMappedPort)
    protected val client get() = rethis

    protected suspend fun connectionProvider() = client.topology.route(PingCommandCodec.encode(Charsets.UTF_8, null))

    protected fun resetClient(new: ReThis) {
        rethis = new
    }
}
