package eu.vendeli.rethis

import com.redis.testcontainers.RedisContainer
import io.kotest.core.spec.style.AnnotationSpec
import kotlinx.coroutines.delay
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

    @Suppress("ktlint:standard:backing-property-naming")
    private var _client = ReThis(redis.host, redis.firstMappedPort)

    protected val client get() = _client

    protected fun resetClient(newClient: ReThis = ReThis(redis.host, redis.firstMappedPort)) {
        _client = newClient
    }

    @AfterAll
    suspend fun afterAll() {
        delay(1000)
    }
}
