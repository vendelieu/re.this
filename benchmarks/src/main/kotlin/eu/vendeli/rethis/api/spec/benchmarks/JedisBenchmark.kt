package eu.vendeli.rethis.api.spec.benchmarks

import com.redis.testcontainers.RedisContainer
import kotlinx.benchmark.*
import kotlinx.coroutines.DelicateCoroutinesApi
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Threads
import org.openjdk.jmh.annotations.Timeout
import org.openjdk.jmh.annotations.Warmup
import org.testcontainers.utility.DockerImageName
import redis.clients.jedis.JedisPooled
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
@Threads(16)
@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Timeout(time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(1, jvmArgsAppend = ["-Xms12g", "-Xmx12g", "-XX:MaxMetaspaceSize=1g"])
class JedisBenchmark {
    private lateinit var jedis: JedisPooled

    private val redis = RedisContainer(
        DockerImageName.parse("redis:7.4.0"),
    )

    @Setup
    fun setup() {
        redis.start()
        jedis = JedisPooled(redis.host, redis.firstMappedPort)
        jedis.ping()
    }

    @TearDown
    fun tearDown() {
        redis.stop()
        jedis.close()
    }

    @Benchmark
    @OptIn(DelicateCoroutinesApi::class)
    fun jedisSetGet(bh: Blackhole) {
        val randInt = (1..10_000).random()

        bh.consume(jedis.set("keyJedis$randInt", "value$randInt"))
        val value = jedis.get("keyJedis$randInt")
        assert(value == "value$randInt")
    }
}
