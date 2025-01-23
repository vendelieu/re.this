package eu.vendeli.rethis.benchmarks

import io.lettuce.core.RedisClient
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlinx.benchmark.*
import kotlinx.coroutines.DelicateCoroutinesApi
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Timeout
import java.util.concurrent.TimeUnit

@OptIn(DelicateCoroutinesApi::class)
@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Timeout(time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(1, jvmArgsAppend = ["-Xms12g", "-Xmx12g", "-Xss2m", "-XX:MaxMetaspaceSize=1g"])
class LettuceBenchmark {
    private lateinit var lettuce: RedisAsyncCommands<String, String>

    @Setup
    fun setup() {
        lettuce = RedisClient.create("redis://localhost").connect().async()
        lettuce.ping().get()
    }

    @TearDown
    fun tearDown() {
        lettuce.shutdown(false)
    }

    @Benchmark
    fun lettuceSetGet(bh: Blackhole) {
        bh.consume(lettuce.set("key", "value").get())
        bh.consume(lettuce.get("key").get())
    }
}
