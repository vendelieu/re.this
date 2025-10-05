package eu.vendeli.rethis.commands.server

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.server.*
import eu.vendeli.rethis.shared.request.server.ReplicaOfArgs
import eu.vendeli.rethis.shared.types.ReThisException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class ServerGeneralCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `CONFIG REWRITE executes`() {
        shouldThrow<ReThisException> {
            client.configRewrite()
        }.cause.shouldNotBeNull().message shouldBe "ERR The server is running without a config file"
    }

    @Test
    suspend fun `LATENCY RESET returns counter`() {
        val res = client.latencyReset()
        res.shouldBeGreaterThanOrEqual(0)
    }

    @Test
    suspend fun `ROLE returns current role info`() {
        val res = client.role()
        res.shouldNotBeEmpty()
    }

    @Test
    suspend fun `SLOWLOG RESET executes`() {
        val res = client.slowLogReset()
        res shouldBe true
    }

    @Test
    suspend fun `REPLICAOF NO ONE is accepted`() {
        val res = client.replicaOf(ReplicaOfArgs.NoOne)
        res shouldNotBe null
    }
}
