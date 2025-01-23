package eu.vendeli.rethis

import com.redis.testcontainers.RedisContainer
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.mpp.start
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.testcontainers.utility.DockerImageName

abstract class ReThisTestCtx(
    private val withJsonModule: Boolean = false,
) : AnnotationSpec() {
    protected val targetDb = 1L
    protected val timestamp: Instant get() = Clock.System.now()

    protected val redis = RedisContainer(
        DockerImageName.parse(if (!withJsonModule) "redis:7.4.0" else "redislabs/rejson"),
    ).apply {
        start()
    }

    @Suppress("ktlint:standard:backing-property-naming")
    private var _client: ReThis? = null

    protected val client: ReThis get() = _client!!

    protected fun resetClient(newClient: ReThis = ReThis(redis.host, redis.firstMappedPort)) {
        _client = newClient
    }

    @BeforeAll
    fun beforeAll() {
        val client = ReThis(redis.host, redis.firstMappedPort)
        resetClient(client)
    }

    @AfterAll
    suspend fun afterAll() {
        delay(1000)
    }
}
