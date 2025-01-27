package eu.vendeli.rethis.benchmarks

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlinx.benchmark.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Timeout
import java.util.concurrent.TimeUnit

@OptIn(DelicateCoroutinesApi::class, ExperimentalLettuceCoroutinesApi::class)
@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Timeout(time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(1, jvmArgsAppend = ["-Xms12g", "-Xmx12g", "-Xss2m", "-XX:MaxMetaspaceSize=1g"])
class LettuceBenchmark {
    private lateinit var lettuce: RedisCoroutinesCommands<String, String>

    @Setup
    fun setup() {
        lettuce = RedisClient.create("redis://localhost").connect().coroutines()

        GlobalScope.launch {
            lettuce.ping()
        }
    }

    @TearDown
    fun tearDown() {
        GlobalScope.launch {
            lettuce.shutdown(false)
        }
    }

    @Benchmark
    fun lettuceSetGet(bh: Blackhole) {
        GlobalScope.launch {
            bh.consume(lettuce.set("key", "value"))
            bh.consume(lettuce.get("key"))
        }
    }
}
