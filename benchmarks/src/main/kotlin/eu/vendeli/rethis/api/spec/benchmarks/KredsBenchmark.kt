//package eu.vendeli.rethis.api.spec.benchmarks
//
//import io.github.crackthecodeabhi.kreds.connection.Endpoint
//import io.github.crackthecodeabhi.kreds.connection.KredsClient
//import io.github.crackthecodeabhi.kreds.connection.newClient
//import io.github.crackthecodeabhi.kreds.connection.shutdown
//import kotlinx.benchmark.Benchmark
//import kotlinx.benchmark.Blackhole
//import kotlinx.benchmark.Setup
//import kotlinx.benchmark.TearDown
//import kotlinx.coroutines.DelicateCoroutinesApi
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.runBlocking
//
//@OptIn(DelicateCoroutinesApi::class)
//class KredsBenchmark : JMHBenchmark() {
//    private lateinit var kreds: KredsClient
//
//    @Setup
//    fun setup() {
//        redis.start()
//        kreds = newClient(Endpoint(redis.host, redis.firstMappedPort))
//        runBlocking {
//            kreds.ping("test")
//        }
//    }
//
//    @TearDown
//    fun tearDown() {
//        redis.stop()
//        runBlocking {
//            kreds.use {
//                shutdown()
//            }
//        }
//    }
//
//    @Benchmark
//    fun kredsSetGet(bh: Blackhole) {
//        val randInt = (1..10_000).random()
//        val key = "keyKreds$randInt"
//        val value = "value$randInt"
//
//        benchScope.launch {
//            bh.consume(kreds.set(key, value))
//            val redisValue = kreds.get(key)
//            assert(redisValue == value)
//        }
//    }
//}
