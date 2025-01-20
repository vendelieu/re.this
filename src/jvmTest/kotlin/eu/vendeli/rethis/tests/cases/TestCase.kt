package eu.vendeli.rethis.tests.cases

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.*
import eu.vendeli.rethis.types.core.RType
import io.ktor.util.collections.*
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TestCase : ReThisTestCtx() {
    @Test
    suspend fun `operations in transaction test case`() {
        val coroutineScope = CoroutineScope(
            Dispatchers.IO + CoroutineName("CustomCoroutineScope"),
        )

        val map1 = ConcurrentMap<Int, Job>()
        val map2 = ConcurrentMap<Int, Job>()

        repeat(1_000) { id ->
            client.transaction {
                hSet(
                    "some:key:$id",
                    "some:field1" to "some-value1-$id",
                    "some:field2" to "some-value2-$id",
                    "some:field3" to "some-value3-$id",
                )

                hPExpire("some:key:$id", 1.minutes, "some-value1-$id")

                sAdd("some:key", id.toString())
            }

            map2[id] = coroutineScope.launch(start = CoroutineStart.LAZY) {
                while (true) {
                    client.hPExpire("some:key:$id", 1.minutes, "some-value1-$id")
                    delay(5.seconds)
                }
            }.also { job ->
                job.invokeOnCompletion {
                    map2.remove(id)
                }

                job.start()
            }

            map1[id] = coroutineScope.launch(start = CoroutineStart.LAZY) {
                delay(10.seconds)

                val value1 = client.hGet("some:key:$id", "some:field1")
                val value2 = client.hGet("some:key:$id", "some:field2")
                val value3 = client.hGet("some:key:$id", "some:field3")

                if (value1 != "some-value1-$id") {
                    throw IllegalArgumentException("value must be 'some-value1-$id', but found '$value1'")
                }

                if (value2 != "some-value2-$id") {
                    throw IllegalArgumentException("value must be 'some-value2-$id', but found '$value2'")
                }

                if (value3 != "some-value3-$id") {
                    throw IllegalArgumentException("value must be 'some-value3-$id', but found '$value3'")
                }

                client.transaction {
                    del("some:key:$id")
                    sRem("some:key", id.toString())
                }
            }.also { job ->
                job.invokeOnCompletion {
                    map1.remove(id)
                    map2[id]?.cancel()
                }

                job.start()
            }
        }
    }


    private val scriptsSha1Map = ConcurrentMap<String, String>()
    internal suspend inline fun ReThis.fastEval(
        scriptId: String,
        script: String,
        numKeys: Long,
        vararg keys: String,
    ): RType {
        var sha1 = scriptsSha1Map[scriptId]

        if (sha1 == null) {
            sha1 = scriptLoad(script) ?: throw NullPointerException("script load don't return sha1 hash!")

            scriptsSha1Map[scriptId] = sha1
        }

        return evalSha(sha1, numKeys, *keys)
    }

    @Test
    suspend fun `script operations test case`() {
        val coroutineScope = CoroutineScope(
            Dispatchers.IO + CoroutineName("CustomCoroutineScope"),
        )

        val map1 = ConcurrentMap<Int, Job>()
        val map2 = ConcurrentMap<Int, Job>()

        repeat(1_000) { id ->
            client.fastEval(
                "script1",
                """
                    redis.call('HSET', KEYS[1], ARGV[2], ARGV[3], ARGV[4], ARGV[5], ARGV[6], ARGV[7])
                    redis.call('HPEXPIRE', KEYS[1], 60000, 'FIELDS', 1, ARGV[2])
                    redis.call('SADD', KEYS[2],  ARGV[1])
                """.trimIndent(),
                2,
                "some:key:$id",
                "some:key",
                id.toString(),
                *mapOf(
                    "some:field1" to "some-value1-$id",
                    "some:field2" to "some-value2-$id",
                    "some:field3" to "some-value3-$id",
                ).flatMap { listOf(it.key, it.value) }.toTypedArray(),
            )

            map2[id] = coroutineScope.launch(start = CoroutineStart.LAZY) {
                while (true) {
                    client.hPExpire("some:key:$id", 1.minutes, "some:field1")
                    delay(5.seconds)
                }
            }.also { job ->
                job.invokeOnCompletion {
                    map2.remove(id)
                }

                job.start()
            }

            map1[id] = coroutineScope.launch(start = CoroutineStart.LAZY) {
                delay(10.seconds)

                val value1 = client.hGet("some:key:$id", "some:field1")
                val value2 = client.hGet("some:key:$id", "some:field2")
                val value3 = client.hGet("some:key:$id", "some:field3")

                if (value1 != "some-value1-$id") {
                    throw IllegalArgumentException("value must be 'some-value1-$id', but found '$value1'")
                }

                if (value2 != "some-value2-$id") {
                    throw IllegalArgumentException("value must be 'some-value2-$id', but found '$value2'")
                }

                if (value3 != "some-value3-$id") {
                    throw IllegalArgumentException("value must be 'some-value3-$id', but found '$value3'")
                }

                client.fastEval(
                    "script2",
                    """
                        redis.call('DEL', KEYS[1])
                        redis.call('SREM', KEYS[2], ARGV[1])
                    """.trimIndent(),
                    2,
                    "some:key:$id",
                    "some:key",
                    id.toString(),
                )
            }.also { job ->
                job.invokeOnCompletion {
                    map1.remove(id)
                    map2[id]?.cancel()
                }

                job.start()
            }
        }
    }
}
