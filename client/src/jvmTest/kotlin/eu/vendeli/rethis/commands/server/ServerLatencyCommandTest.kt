package eu.vendeli.rethis.commands.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.server.*
import eu.vendeli.rethis.command.string.set
import eu.vendeli.rethis.shared.request.string.KeyValue
import eu.vendeli.rethis.shared.types.BulkString
import eu.vendeli.rethis.shared.types.Int64
import eu.vendeli.rethis.shared.types.RArray
import eu.vendeli.rethis.shared.types.RMap
import eu.vendeli.rethis.shared.types.ReThisException
import eu.vendeli.rethis.types.common.RespVer
import eu.vendeli.rethis.utils.execute
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeTypeOf

class ServerLatencyCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `LATENCY DOCTOR reports the sampled command event`() {
        withLatencyEvent(client) {
            client.latencyDoctor() shouldContain "command"
        }
    }

    @Test
    suspend fun `LATENCY DOCTOR reports disabled monitoring when no threshold is set`() {
        client.latencyDoctor() shouldContain "Latency monitoring is disabled"
    }

    @Test
    suspend fun `LATENCY LATEST returns the sampled command event`() {
        client.latencyReset()
        client.latencyLatest().shouldBeEmpty()

        withLatencyEvent(client) {
            val latest = client.latencyLatest()
            latest shouldHaveSize 1
            val event = latest.single().shouldBeTypeOf<RArray>().value
            event shouldHaveSize 4
            event[0] shouldBe BulkString("command")
            event[2].shouldBeTypeOf<Int64>().value shouldBeGreaterThanOrEqual 1L
            event[3].shouldBeTypeOf<Int64>().value shouldBeGreaterThanOrEqual 1L
        }
    }

    @Test
    suspend fun `LATENCY RESET clears the sampled command event`() {
        withLatencyEvent(client) {
            client.latencyReset("command") shouldBe 1L
            client.latencyReset("command") shouldBe 0L
            client.latencyLatest().shouldBeEmpty()
        }
    }

    @Test
    suspend fun `LATENCY GRAPH renders sampled command event over RESP2`() {
        val resp2 = createClient(protocol = RespVer.V2)
        try {
            withLatencyEvent(resp2) {
                resp2.latencyGraph("command") shouldContain "command - high"
            }
        } finally {
            resp2.close()
        }
    }

    @Test
    suspend fun `LATENCY GRAPH renders sampled command event over RESP3`() {
        withLatencyEvent(client) {
            client.latencyGraph("command") shouldContain "command - high"
        }
    }

    @Test
    suspend fun `LATENCY GRAPH reports missing samples for unknown event`() {
        val ex = shouldThrow<ReThisException> {
            client.latencyGraph("no-such-event")
        }
        ex.message.shouldNotBeNull() shouldContain "No samples available for event 'no-such-event'"
    }

    @Test
    suspend fun `LATENCY HISTORY returns samples of the command event`() {
        client.latencyHistory("no-such-event").shouldBeEmpty()

        withLatencyEvent(client) {
            val history = client.latencyHistory("command")
            history shouldHaveSize 1
            val sample = history.single().shouldBeTypeOf<RArray>().value
            sample shouldHaveSize 2
            sample[0].shouldBeTypeOf<Int64>()
            sample[1].shouldBeTypeOf<Int64>().value shouldBeGreaterThanOrEqual 1L
        }
    }

    @Test
    suspend fun `LATENCY HISTOGRAM returns call counts for the requested command`() {
        client.set("latency:histogram:key", "value")
        client.set("latency:histogram:key", "value")

        val histogram = client.latencyHistogram("set")
        histogram.keys shouldBe setOf("set")
        val set = histogram.getValue("set").shouldBeTypeOf<RMap>().value
        set[BulkString("calls")].shouldBeTypeOf<Int64>().value shouldBeGreaterThanOrEqual 2L
        set[BulkString("histogram_usec")].shouldBeTypeOf<RMap>()

        client.latencyHistogram("no-such-command").isEmpty() shouldBe true
    }

    private suspend fun withLatencyEvent(target: ReThis, block: suspend () -> Unit) {
        target.configSet(KeyValue("latency-monitor-threshold", "1"))
        try {
            target.latencyReset("command")
            target.busyLoopScript()
            block()
        } finally {
            runCatching { target.latencyReset("command") }
            target.configSet(KeyValue("latency-monitor-threshold", "0"))
        }
    }

    // a counting loop in a script so a >1ms command sample is recorded without the DEBUG command;
    // scripts see a frozen clock, so the loop is bounded by iterations and not by elapsed time
    private suspend fun ReThis.busyLoopScript() {
        execute {
            add("EVAL")
            add("local x = 0; for i = 1, 30000000 do x = x + i end; return x")
            add(0)
        }
    }
}
