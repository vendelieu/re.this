package eu.vendeli.rethis.api.spec.benchmarks

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.command.connection.ping
import eu.vendeli.rethis.command.string.get
import eu.vendeli.rethis.command.string.set
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole


@OptIn(DelicateCoroutinesApi::class)
class RethisBenchmark : JMHBenchmark() {
    private lateinit var rethis: ReThis

    @Setup
    fun setup() {
        redis.start()
        rethis = ReThis(redis.host, redis.firstMappedPort)
        runBlocking { rethis.ping("test") }
    }

    @TearDown
    fun tearDown() {
        redis.stop()
        rethis.close()
    }

    @Benchmark
    fun rethisSetGet(bh: Blackhole) {
        val randInt = random.nextInt()
        val key = "keyReThis$randInt"
        val value = "value$randInt"

        runBlocking {
            bh.consume(rethis.set(key, value))
            val redisValue = rethis.get(key)
            assert(redisValue == value)
        }
    }
}
