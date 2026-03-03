package eu.vendeli.rethis.api.spec.benchmarks

import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.connection.newClient
import io.github.crackthecodeabhi.kreds.connection.shutdown
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.TearDown
import org.openjdk.jmh.infra.Blackhole

class KredsBenchmark : JMHBenchmark() {
    private lateinit var kreds: KredsClient


    @Setup(Level.Trial)
    fun setup() {
        redis.start()
        kreds = newClient(Endpoint(redis.host, redis.firstMappedPort))
        runBlocking {
            kreds.ping("test")
        }
    }

    @TearDown(Level.Trial)
    fun tearDown() {
        runBlocking {
            kreds.use {
                shutdown()
            }
        }
        redis.stop()
        benchScope.cancel()
    }

    @Benchmark
    fun kredsSetGet(bh: Blackhole) = runBlocking {
        // Launch concurrent coroutines within benchScope
        val jobs = List(opsCount) { // number of parallel coroutines
            benchScope.async {
                val randInt = random.nextInt()
                val key = "keyKreds$randInt"
                val value = "value$randInt"

                bh.consume(kreds.set(key, value))
                val redisValue = kreds.get(key)
                assert(redisValue == value)
            }
        }
        jobs.awaitAll() // wait for all coroutines to finish before JMH counts the iteration
    }
}
