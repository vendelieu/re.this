package eu.vendeli.rethis.commands.server

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.server.*
import eu.vendeli.rethis.command.string.set
import io.kotest.matchers.shouldNotBe

class ServerMemoryCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `MEMORY STATS returns map`() {
        val res = client.memoryStats()
        res shouldNotBe null
    }

    @Test
    suspend fun `MEMORY DOCTOR returns advice string`() {
        val res = client.memoryDoctor()
        res shouldNotBe null
    }

    @Test
    suspend fun `MEMORY PURGE triggers purge`() {
        val res = client.memoryPurge()
        res shouldNotBe null
    }

    @Test
    suspend fun `MEMORY MALLOC-STATS returns info`() {
        val res = client.memoryMallocStats()
        res shouldNotBe null
    }

    @Test
    suspend fun `MEMORY USAGE returns usage for an existing key`() {
        val key = "server:memory:key"
        client.set(key, "value")
        val res = client.memoryUsage(key)
        res shouldNotBe null
    }
}
