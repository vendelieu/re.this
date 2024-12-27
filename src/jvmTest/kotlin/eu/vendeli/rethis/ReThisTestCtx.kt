package eu.vendeli.rethis

import com.redis.testcontainers.RedisContainer
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.testcontainers.utility.DockerImageName

abstract class ReThisTestCtx(
    withJsonModule: Boolean = false,
) {
    protected val targetDb = 1L
    protected val timestamp: Instant get() = Clock.System.now()

    protected val redis = RedisContainer(
        DockerImageName.parse(if (!withJsonModule) "redis:7.4.0" else "redislabs/rejson"),
    ).apply {
        start()
    }

    protected val client = ReThis(redis.host, redis.firstMappedPort)
}
