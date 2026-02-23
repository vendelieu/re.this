//package eu.vendeli.rethis.api.spec.benchmarks
//
//import kotlinx.benchmark.Benchmark
//import kotlinx.benchmark.Blackhole
//import kotlinx.benchmark.Setup
//import kotlinx.benchmark.TearDown
//import kotlinx.coroutines.DelicateCoroutinesApi
//import redis.clients.jedis.RedisClient
//
//@DelicateCoroutinesApi
//class JedisBenchmark : JMHBenchmark() {
//    private lateinit var jedis: RedisClient
//
//    @Setup
//    fun setup() {
//        redis.start()
//        jedis = RedisClient.create(redis.host, redis.firstMappedPort)
//        jedis.ping()
//    }
//
//    @TearDown
//    fun tearDown() {
//        redis.stop()
//        jedis.close()
//    }
//
//    @Benchmark
//    @OptIn(DelicateCoroutinesApi::class)
//    fun jedisSetGet(bh: Blackhole) {
//        val randInt = (1..10_000).random()
//        val key = "keyJedis$randInt"
//        val value = "value$randInt"
//
//        bh.consume(jedis.set(key, value))
//        val redisValue = jedis.get(key)
//        assert(redisValue == value)
//    }
//}
