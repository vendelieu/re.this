package eu.vendeli.rethis.api.spec.benchmarks

import com.redis.testcontainers.RedisContainer
import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.connection.newClient
import io.github.crackthecodeabhi.kreds.connection.shutdown
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Blackhole
import kotlinx.benchmark.Setup
import kotlinx.benchmark.TearDown
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*
import org.testcontainers.utility.DockerImageName
import java.util.concurrent.TimeUnit

@DelicateCoroutinesApi
@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
@Threads(16)
@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Timeout(time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(1, jvmArgsAppend = ["-Xms12g", "-Xmx12g", "-XX:MaxMetaspaceSize=1g"])
class KredsBenchmark {
    private lateinit var kreds: KredsClient
    private val redis = RedisContainer(
        DockerImageName.parse("redis:7.4.0"),
    )

    @Setup
    fun setup() {
        redis.start()
        kreds = newClient(Endpoint(redis.host, redis.firstMappedPort))
        GlobalScope.launch {
            kreds.ping("test")
        }
    }

    @TearDown
    fun tearDown() {
        redis.stop()
        runBlocking {
            kreds.use {
                shutdown()
            }
        }
    }

    @Benchmark
    fun kredsSetGet(bh: Blackhole) {
        val randInt = (1..10_000).random()

        GlobalScope.launch {
            bh.consume(kreds.set("keyKreds$randInt", "value$randInt"))
            val value = kreds.get("keyKreds$randInt")
            assert(value == "value$randInt")
        }
    }
}
