package eu.vendeli.rethis.benchmarks

import kotlinx.benchmark.*
import org.openjdk.jmh.annotations.Timeout
import redis.clients.jedis.JedisPooled
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.Throughput)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Timeout(time = 10, timeUnit = TimeUnit.SECONDS)
class JedisBenchmark {
    private lateinit var jedis: JedisPooled

    @Setup
    fun setup() {
        jedis = JedisPooled("localhost", 6379)
        jedis.ping()
    }

    @TearDown
    fun tearDown() {
        jedis.close()
    }

    @Benchmark
    fun jedisSet(bh: Blackhole) {
        bh.consume(jedis.runCatching { set("key", "value") })
    }

    @Benchmark
    fun jedisGet(bh: Blackhole) {
        bh.consume(jedis.runCatching { get("key") })
    }
}
