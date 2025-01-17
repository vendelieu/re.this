package eu.vendeli.rethis.benchmarks

import io.lettuce.core.RedisClient
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.benchmark.*
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import org.openjdk.jmh.annotations.Timeout
import java.util.concurrent.TimeUnit

@OptIn(DelicateCoroutinesApi::class)
@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Timeout(time = 10, timeUnit = TimeUnit.SECONDS)
class LettuceBenchmark {
    private lateinit var lettuce: RedisAsyncCommands<String, String>

    @Setup
    fun setup() {
        lettuce = RedisClient.create("redis://localhost").connect().async()
        lettuce.ping().await(5, TimeUnit.SECONDS)
    }

    @TearDown
    fun tearDown() {
        lettuce.quit()
    }

    @Benchmark
    fun lettuceSet(bh: Blackhole) {
        GlobalScope.launch(Dispatchers.IO) {
            bh.consume(lettuce.runCatching { set("key", "value").await() })
        }
    }

    @Benchmark
    fun lettuceGet(bh: Blackhole) {
        GlobalScope.launch(Dispatchers.IO) {
            bh.consume(lettuce.runCatching { get("key").await() })
        }
    }
}
