package eu.vendeli.rethis.commands.server

import com.redis.testcontainers.RedisContainer
import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.server.monitor
import eu.vendeli.rethis.shared.types.PlainString
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.testcontainers.utility.DockerImageName

class ServerMonitorCommandTest : AnnotationSpec() {
    @Test
    suspend fun `MONITOR enters monitor mode and responds`() {
        val container = RedisContainer(DockerImageName.parse("redis:7.4.0"))
        try {
            container.start()
            val client = ReThis(container.host, container.firstMappedPort)
            try {
                val res = client.monitor()
                res shouldBe PlainString("OK")
            } finally {
                client.close()
            }
        } finally {
            runCatching { container.stop() }
        }
    }
}
