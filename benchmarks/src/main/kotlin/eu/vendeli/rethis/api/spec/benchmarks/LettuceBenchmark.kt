package eu.vendeli.rethis.api.spec.benchmarks

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.TearDown
import org.openjdk.jmh.infra.Blackhole

@ExperimentalLettuceCoroutinesApi
class LettuceBenchmark : JMHBenchmark() {
    private lateinit var lettuce: RedisCoroutinesCommands<String, String>
    private lateinit var client: RedisClient

    @Setup(Level.Trial)
    fun setup() {
        redis.start()
        client = RedisClient.create("redis://${redis.host}:${redis.firstMappedPort}")
        lettuce = client.connect().coroutines()

        runBlocking {
            lettuce.ping()
        }
    }

    @TearDown(Level.Trial)
    fun tearDown() {
        client.shutdown()
        redis.stop()
        benchScope.cancel()
    }

    @Benchmark
    fun lettuceSetGet(bh: Blackhole) = runBlocking {
        // Launch concurrent coroutines within benchScope
        val jobs = List(opsCount) { // number of parallel coroutines
            val randInt = random.nextInt()
            val key = "keyLettuce$randInt"
            val value = "value$randInt"

            bh.consume(lettuce.set(key, value))
            val redisValue = lettuce.get(key)
            assert(redisValue == value)
        }
        bh.consume(jobs)
    }
}
