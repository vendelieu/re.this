package eu.vendeli.rethis.api.spec.benchmarks

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.connection.ping
import eu.vendeli.rethis.command.string.get
import eu.vendeli.rethis.command.string.set
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.TearDown
import org.openjdk.jmh.infra.Blackhole

class RethisBenchmark : JMHBenchmark() {
    private lateinit var rethis: ReThis

    @Setup(Level.Trial)
    fun setup() {
        // Start Redis container once per trial
        redis.start()
        rethis = ReThis(redis.host, redis.firstMappedPort)
        runBlocking { rethis.ping("test") }
    }

    @TearDown(Level.Trial)
    fun tearDown() {
        rethis.close()
        redis.stop()
        benchScope.cancel()
    }

    @Benchmark
    fun rethisSetGet(bh: Blackhole) = runBlocking {
        // Launch concurrent coroutines within benchScope
        val jobs = List(opsCount) { // number of parallel coroutines
            benchScope.async {
                val randInt = random.nextInt()
                val key = "keyReThis$randInt"
                val value = "value$randInt"

                bh.consume(rethis.set(key, value))
                val redisValue = rethis.get(key)
                assert(redisValue == value)
            }
        }
        jobs.awaitAll() // wait for all coroutines to finish before JMH counts the iteration
    }
}
