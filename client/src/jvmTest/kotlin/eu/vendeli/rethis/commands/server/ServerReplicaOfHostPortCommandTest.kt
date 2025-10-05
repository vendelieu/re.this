package eu.vendeli.rethis.commands.server

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.server.replicaOf
import eu.vendeli.rethis.shared.request.server.ReplicaOfArgs
import io.kotest.matchers.shouldBe

class ServerReplicaOfHostPortCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `REPLICAOF self host-port returns an error`() {
        val result = client.replicaOf(ReplicaOfArgs.HostPort(redis.host, redis.firstMappedPort.toLong()))
        result shouldBe true
    }
}
