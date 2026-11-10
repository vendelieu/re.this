package eu.vendeli.rethis.commands.server

import com.redis.testcontainers.RedisContainer
import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.TestEnv.TARGET_REDIS_VER
import eu.vendeli.rethis.command.server.replicaOf
import eu.vendeli.rethis.shared.request.server.ReplicaOfArgs
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import org.testcontainers.utility.DockerImageName

class ServerReplicaOfHostPortCommandTest : AnnotationSpec() {
    @Test
    suspend fun `REPLICAOF self host-port`() {
        // REPLICAOF changes server-wide replication state. Running it against the shared
        // fixture can leave Redis in a transient sync state that starves unrelated tests.
        val container = RedisContainer(DockerImageName.parse("redis:$TARGET_REDIS_VER"))
        try {
            container.start()
            val client = ReThis(container.host, container.firstMappedPort)
            try {
                client.replicaOf(
                    ReplicaOfArgs.HostPort(container.host, container.firstMappedPort.toLong()),
                ) shouldBe true

                client.replicaOf(ReplicaOfArgs.NoOne) shouldBe true
            } finally {
                runCatching { client.close() }
            }
        } finally {
            runCatching { container.stop() }
        }
    }
}
