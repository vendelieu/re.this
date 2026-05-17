package eu.vendeli.rethis.metrics

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.string.get
import eu.vendeli.rethis.command.string.set
import eu.vendeli.rethis.types.interfaces.CommandOutcome
import eu.vendeli.rethis.types.interfaces.ExperimentalReThisMetricsApi
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalReThisMetricsApi::class)
class MetricsTest : ReThisTestCtx() {
    @Test
    fun `default recorder is null (metrics are off)`() {
        client.cfg.metricsRecorder.shouldBeNull()
    }

    @Test
    fun `single command emits one OK completion with the redis verb`() = runWithRecorder { recorder, c ->
        c.set("metrics:single", "value")

        val completions = recorder.events
            .filterIsInstance<RecordingMetricsRecorder.MetricEvent.CommandCompleted>()
        completions shouldHaveAtLeastSize 1
        val last = completions.last()
        last.command shouldBe "SET"
        last.outcome shouldBe CommandOutcome.OK
        last.attempts shouldBe 1
    }

    @Test
    fun `command flow records pool acquire and release`() = runWithRecorder { recorder, c ->
        c.set("metrics:pool", "x")
        c.get("metrics:pool")

        recorder
            .count<RecordingMetricsRecorder.MetricEvent.Acquired>()
            .shouldBeGreaterThanOrEqual(1)
        recorder
            .count<RecordingMetricsRecorder.MetricEvent.ConnectionReleased>()
            .shouldBeGreaterThanOrEqual(1)
    }

    @Test
    fun `pool initialization emits onConnectionCreated success events`() = runWithRecorder { recorder, _ ->
        recorder
            .count<RecordingMetricsRecorder.MetricEvent.ConnectionCreated>()
            .shouldBeGreaterThanOrEqual(1)
    }

    private fun runWithRecorder(block: suspend (RecordingMetricsRecorder, ReThis) -> Unit) =
        runBlocking {
            val recorder = RecordingMetricsRecorder()
            val c = createClient {
                metricsRecorder = recorder
            }
            try {
                // warm-up: ensure at least one connection has been created before assertions
                c.set("metrics:warmup", "1")
                block(recorder, c)
            } finally {
                runCatching { c.close() }
            }
        }
}
