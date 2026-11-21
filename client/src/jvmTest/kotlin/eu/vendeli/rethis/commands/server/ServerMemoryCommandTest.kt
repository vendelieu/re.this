package eu.vendeli.rethis.commands.server

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.server.*
import eu.vendeli.rethis.command.string.set
import eu.vendeli.rethis.shared.types.Int64
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeBlank
import io.kotest.matchers.types.shouldBeTypeOf

private const val BIG_VALUE_LENGTH = 4096

class ServerMemoryCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `MEMORY STATS returns map`() {
        val res = client.memoryStats()
        res.keys shouldContainAll listOf("peak.allocated", "total.allocated", "keys.count", "dataset.bytes")
        res.getValue("total.allocated").shouldBeTypeOf<Int64>().value shouldBeGreaterThan 0L
    }

    @Test
    suspend fun `MEMORY DOCTOR returns advice string`() {
        client.memoryDoctor().shouldNotBeBlank()
    }

    @Test
    suspend fun `MEMORY PURGE triggers purge`() {
        client.memoryPurge() shouldBe true
    }

    @Test
    suspend fun `MEMORY MALLOC-STATS returns info`() {
        client.memoryMallocStats() shouldContain "jemalloc statistics"
    }

    @Test
    suspend fun `MEMORY USAGE returns usage for an existing key`() {
        val key = "server:memory:key"
        val bigKey = "server:memory:big-key"
        client.set(key, "value")
        client.set(bigKey, "v".repeat(BIG_VALUE_LENGTH))
        val small = client.memoryUsage(key).shouldNotBeNull()
        val big = client.memoryUsage(bigKey).shouldNotBeNull()
        small shouldBeGreaterThan 0L
        big shouldBeGreaterThan small
    }
}
