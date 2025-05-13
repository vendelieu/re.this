package eu.vendeli.rethis.api.spec.benchmarks

import com.redis.testcontainers.RedisContainer
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
import org.testcontainers.utility.DockerImageName
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
    private val redis = RedisContainer(
        DockerImageName.parse("redis:7.4.0"),
    )

    @Setup
    fun setup() {
        redis.start()
        lettuce = RedisClient.create("redis://${redis.host}:${redis.firstMappedPort}").connect().coroutines()

        GlobalScope.launch {
            lettuce.ping()
        }
    }

    @TearDown
    fun tearDown() {
        redis.stop()
        GlobalScope.launch {
            lettuce.shutdown(false)
        }
    }

    @Benchmark
    fun lettuceSetGet(bh: Blackhole) {
        val randInt = (1..10_000).random()

        GlobalScope.launch {
            bh.consume(lettuce.set("keyLettuce$randInt", "value$randInt"))
            val value = lettuce.get("key")
            assert(value == "value$randInt")
        }
    }
}
