package eu.vendeli.rethis.commands.server

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.server.*
import eu.vendeli.rethis.shared.types.UnexpectedResponseType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldNotBe

class ServerLatencyCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `LATENCY DOCTOR returns diagnostics`() {
        val res = client.latencyDoctor()
        res shouldNotBe null
    }

    @Test
    suspend fun `LATENCY LATEST returns latest events or empty list`() {
        val res = client.latencyLatest()
        res shouldNotBe null
    }

    @Test
    suspend fun `LATENCY RESET resets events`() {
        val res = client.latencyReset()
        res shouldNotBe null
    }

    @Test
    suspend fun `LATENCY GRAPH returns graph or empty`() {
        shouldThrow<UnexpectedResponseType> {
            client.latencyGraph("command")
        }.cause.shouldNotBeNull().message shouldNotBe "-ERR No samples available for event 'command'"
    }

    @Test
    suspend fun `LATENCY HISTORY returns history or empty`() {
        val res = client.latencyHistory("command")
        res shouldNotBe null
    }

    @Test
    suspend fun `LATENCY HISTOGRAM returns histogram or empty`() {
        val res = client.latencyHistogram()
        res shouldNotBe null
    }
}
