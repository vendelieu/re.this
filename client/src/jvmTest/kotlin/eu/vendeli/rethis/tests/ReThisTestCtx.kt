package eu.vendeli.rethis.tests

import com.redis.testcontainers.RedisContainer
import eu.vendeli.rethis.spec.ReThis
import io.kotest.core.spec.style.AnnotationSpec
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.testcontainers.utility.DockerImageName

abstract class ReThisTestCtx(
    withJsonModule: Boolean = false,
) : AnnotationSpec() {
    protected val targetDb = 1L
    protected val timestamp: Instant get() = Clock.System.now()

    protected val redis = RedisContainer(
        DockerImageName.parse(if (!withJsonModule) "redis:7.4.0" else "redislabs/rejson"),
    ).apply {
        start()
    }

    private var rethis: ReThis = ReThis(redis.host, redis.firstMappedPort)
    protected val client get() = rethis

    protected fun resetClient(new: ReThis) {
        rethis = new
    }
}
