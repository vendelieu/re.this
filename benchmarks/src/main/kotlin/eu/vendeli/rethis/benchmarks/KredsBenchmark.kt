package eu.vendeli.rethis.benchmarks

import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.connection.newClient
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Blackhole
import kotlinx.benchmark.Setup
import kotlinx.benchmark.TearDown
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@DelicateCoroutinesApi
@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Timeout(time = 10, timeUnit = TimeUnit.SECONDS)
class KredsBenchmark {
    private lateinit var kreds: KredsClient

    @Setup
    fun setup() {
        kreds = newClient(Endpoint("localhost", 6379))
        GlobalScope.launch {
//            kreds.use { it.ping("test") }
            kreds.ping("test")
        }
    }

    @TearDown
    fun tearDown() {
        kreds.close()
    }

    @Benchmark
    fun kredsSet(bh: Blackhole) {
        GlobalScope.launch {
            bh.consume(
                kreds.set("key", "value"),
//                kreds.use { it.set("key", "value") },
            )
        }
    }

    @Benchmark
    fun kredsGet(bh: Blackhole) {
        GlobalScope.launch {
            bh.consume(
//            kreds.use { it.get("key") }
                kreds.get("key"),
            )
        }
    }
}
