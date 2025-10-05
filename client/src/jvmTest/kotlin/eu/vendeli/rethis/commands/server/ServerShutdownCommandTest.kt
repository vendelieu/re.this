package eu.vendeli.rethis.commands.server

import com.redis.testcontainers.RedisContainer
import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.server.shutdown
import eu.vendeli.rethis.shared.request.server.SaveSelector
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.nulls.shouldBeNull
import org.testcontainers.utility.DockerImageName

class ServerShutdownCommandTest : AnnotationSpec() {
    @Test
    suspend fun `SHUTDOWN NOSAVE terminates the server connection`() {
        val container = RedisContainer(DockerImageName.parse("redis:7.4.0"))
        try {
            container.start()
            val client = ReThis(container.host, container.firstMappedPort)

            shouldNotThrowAny {
                client.shutdown(SaveSelector.NOSAVE).shouldBeNull()
            }
        } finally {
            runCatching { container.stop() }
        }
    }
}
