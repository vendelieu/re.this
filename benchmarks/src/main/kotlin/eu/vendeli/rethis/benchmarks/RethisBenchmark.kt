package eu.vendeli.rethis.benchmarks

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.commands.get
import eu.vendeli.rethis.commands.ping
import eu.vendeli.rethis.commands.set
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

@DelicateCoroutinesApi
@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Timeout(time = 10, timeUnit = TimeUnit.SECONDS)
class RethisBenchmark {
    private lateinit var rethis: ReThis

    @Setup
    fun setup() {
        rethis = ReThis("localhost", 6379)
        GlobalScope.launch { rethis.ping("test") }
    }

    @TearDown
    fun tearDown() {
        rethis.disconnect()
    }

    @Benchmark
    fun rethisSet(bh: Blackhole) {
        GlobalScope.launch {
            bh.consume(rethis.runCatching { set("key", "value") })
        }
    }

    @Benchmark
    fun rethisGet(bh: Blackhole) {
        GlobalScope.launch {
            bh.consume(rethis.runCatching { get("key") })
        }
    }
}
