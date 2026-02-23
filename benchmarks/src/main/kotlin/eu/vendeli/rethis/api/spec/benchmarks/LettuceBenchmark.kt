//package eu.vendeli.rethis.api.spec.benchmarks
//
//import io.lettuce.core.ExperimentalLettuceCoroutinesApi
//import io.lettuce.core.RedisClient
//import io.lettuce.core.api.coroutines
//import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
//import kotlinx.benchmark.Benchmark
//import kotlinx.benchmark.Blackhole
//import kotlinx.benchmark.Setup
//import kotlinx.benchmark.TearDown
//import kotlinx.coroutines.DelicateCoroutinesApi
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.runBlocking
//
//@ExperimentalLettuceCoroutinesApi
//@OptIn(DelicateCoroutinesApi::class)
//class LettuceBenchmark : JMHBenchmark() {
//    private lateinit var lettuce: RedisCoroutinesCommands<String, String>
//
//    @Setup
//    fun setup() {
//        redis.start()
//        lettuce = RedisClient.create("redis://${redis.host}:${redis.firstMappedPort}").connect().coroutines()
//
//        runBlocking {
//            lettuce.ping()
//        }
//    }
//
//    @TearDown
//    fun tearDown() {
//        redis.stop()
//        runBlocking {
//            lettuce.shutdown(false)
//        }
//    }
//
//    @Benchmark
//    fun lettuceSetGet(bh: Blackhole) {
//        val randInt = (1..10_000).random()
//        val key = "keyLettuce$randInt"
//        val value = "value$randInt"
//
//        benchScope.launch {
//            bh.consume(lettuce.set(key, value))
//            val redisValue = lettuce.get(key)
//            assert(redisValue == value)
//        }
//    }
//}
