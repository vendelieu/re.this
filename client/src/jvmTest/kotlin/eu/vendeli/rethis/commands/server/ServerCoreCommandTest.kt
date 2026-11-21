package eu.vendeli.rethis.commands.server

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.connection.ping
import eu.vendeli.rethis.command.server.*
import eu.vendeli.rethis.shared.request.string.KeyValue
import eu.vendeli.rethis.shared.types.BulkString
import eu.vendeli.rethis.shared.types.PlainString
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.io.readString

class ServerCoreCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `COMMAND COUNT returns a positive number`() {
        client.commandCount() shouldBeGreaterThan 0L
    }

    @Test
    suspend fun `TIME returns unix seconds and microseconds`() {
        val res = client.time()

        res shouldHaveSize 2
        res.forEach { it shouldMatch Regex("\\d+") }
        res.first().toLong() shouldBeGreaterThan 0L
        res.last().toLong() shouldBeGreaterThanOrEqual 0L
    }

    @Test
    suspend fun `ROLE reports master first for a standalone server`() {
        val res = client.role()

        res.first().let {
            when (it) {
                is BulkString -> it.value.readString()
                is PlainString -> it.value
                else -> error("Unexpected role element type: $it")
            }
        } shouldBe "master"
    }

    @Test
    suspend fun `LASTSAVE returns a positive timestamp`() {
        client.lastSave() shouldBeGreaterThan 0L
    }

    @Test
    suspend fun `SLOWLOG records commands and RESET clears them`() {
        client.configSet(KeyValue("slowlog-log-slower-than", "0"))
        try {
            client.slowLogReset() shouldBe true
            client.ping()

            client.slowLogLen() shouldBeGreaterThan 0L
            client.slowLogGet(count = 1) shouldHaveSize 1
        } finally {
            client.configSet(KeyValue("slowlog-log-slower-than", DEFAULT_SLOWLOG_THRESHOLD_MICROS))
        }

        client.slowLogReset() shouldBe true
        client.slowLogLen() shouldBe 0L
        client.slowLogGet() shouldHaveSize 0
    }

    private companion object {
        const val DEFAULT_SLOWLOG_THRESHOLD_MICROS = "10000"
    }
}
