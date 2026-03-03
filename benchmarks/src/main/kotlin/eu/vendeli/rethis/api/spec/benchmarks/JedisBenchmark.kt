package eu.vendeli.rethis.api.spec.benchmarks

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.TearDown
import org.openjdk.jmh.infra.Blackhole
import redis.clients.jedis.JedisPooled

class JedisBenchmark : JMHBenchmark() {
    private lateinit var jedis: JedisPooled

    @Setup(Level.Trial)
    fun setup() {
        redis.start()
        jedis = JedisPooled(redis.host, redis.firstMappedPort)
        jedis.ping()
    }

    @TearDown(Level.Trial)
    fun tearDown() {
        jedis.close()
        redis.stop()
        benchScope.cancel()
    }

    @Benchmark
    fun jedisSetGet(bh: Blackhole) = runBlocking {
        // Launch concurrent coroutines within benchScope
        val jobs = List(opsCount) { // number of parallel coroutines
            benchScope.async {
                val randInt = random.nextInt()
                val key = "keyJedis$randInt"
                val value = "value$randInt"

                bh.consume(jedis.set(key, value))
                val redisValue = jedis.get(key)
                assert(redisValue == value)
            }
        }
        jobs.awaitAll() // wait for all coroutines to finish before JMH counts the iteration
    }
}
