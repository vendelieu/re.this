package eu.vendeli.rethis.api.spec.benchmarks

import com.redis.testcontainers.RedisContainer
import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.connection.ping
import eu.vendeli.rethis.command.string.get
import eu.vendeli.rethis.command.string.set
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import org.testcontainers.utility.DockerImageName
import java.util.concurrent.TimeUnit

@DelicateCoroutinesApi
@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
@Threads(8)
@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Timeout(time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(1, jvmArgsAppend = ["-Xms8g", "-Xmx8g", "-XX:MaxMetaspaceSize=1g"])
class RethisBenchmark {
    private lateinit var rethis: ReThis
    private val redis = RedisContainer(
        DockerImageName.parse("redis:7.4.0"),
    )

    @Setup
    fun setup() {
        redis.start()
        rethis = ReThis(redis.host, redis.firstMappedPort)
        GlobalScope.launch { rethis.ping("test") }
    }

    @TearDown
    fun tearDown() {
        redis.stop()
        runBlocking { rethis.close() }
    }

    @Benchmark
    fun rethisSetGet(bh: Blackhole) {
        val randInt = (1..10_000).random()

        GlobalScope.launch {
            bh.consume(rethis.set("keyReThis$randInt", "value$randInt"))
            val value = rethis.get("keyReThis$randInt")
            assert(value == "value$randInt")
        }
    }
}
