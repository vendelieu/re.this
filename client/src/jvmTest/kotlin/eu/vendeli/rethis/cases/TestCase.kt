package eu.vendeli.rethis.cases

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.generic.del
import eu.vendeli.rethis.command.hash.hExpire
import eu.vendeli.rethis.command.hash.hGet
import eu.vendeli.rethis.command.hash.hSet
import eu.vendeli.rethis.command.scripting.evalSha
import eu.vendeli.rethis.command.scripting.scriptLoad
import eu.vendeli.rethis.command.set.sAdd
import eu.vendeli.rethis.command.set.sRem
import eu.vendeli.rethis.shared.request.common.FieldValue
import eu.vendeli.rethis.shared.types.RType
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.matchers.shouldBe
import io.ktor.util.collections.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TestCase : ReThisTestCtx() {
    @Test
    suspend fun `operations in transaction test case`() {
        val mainJob = Job()
        val coroutineScope = CoroutineScope(
            Dispatchers.IO + CoroutineName("CustomCoroutineScope") + mainJob,
        )

        val map1 = ConcurrentMap<Int, Job>()
        val map2 = ConcurrentMap<Int, Job>()

        val totalRepeats = 1_000
        repeat(totalRepeats) { id ->
            client.transaction {
                hSet(
                    "some:key:$id",
                    FieldValue("some:field1", "some-value1-$id"),
                    FieldValue("some:field2", "some-value2-$id"),
                    FieldValue("some:field3", "some-value3-$id"),
                )

                hExpire("some:key:$id", 1.minutes, "some-value1-$id")

                sAdd("some:key", id.toString())
            }

            map2[id] = coroutineScope
                .launch(start = CoroutineStart.LAZY) {
                    repeat(totalRepeats) {
                        client.hExpire("some:key:$id", 1.minutes, "some-value1-$id")
                        delay(2.seconds)
                    }
                }.also { job ->
                    job.invokeOnCompletion {
                        map2.remove(id)
                    }

                    job.start()
                }

            map1[id] = coroutineScope
                .launch(start = CoroutineStart.LAZY) {
                    delay(10.seconds)

                    eventually(1.seconds) {
                        client.hGet("some:key:$id", "some:field1") shouldBe "some-value1-$id"
                    }
                    eventually(1.seconds) {
                        client.hGet("some:key:$id", "some:field2") shouldBe "some-value2-$id"
                    }
                    eventually(1.seconds) {
                        client.hGet("some:key:$id", "some:field3") shouldBe "some-value3-$id"
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

        joinAll(*mainJob.children.toList().toTypedArray())
    }

    private val scriptsSha1Map = ConcurrentMap<String, String>()
    private val scriptLoadMutex = Mutex()

    internal suspend fun ReThis.fastEval(
        scriptId: String,
        script: String,
        vararg keys: String,
    ): RType {
        val sha1 = scriptsSha1Map[scriptId] ?: scriptLoadMutex.withLock {
            // Double-check inside the lock
            scriptsSha1Map[scriptId] ?: scriptLoad(script).also {
                scriptsSha1Map[scriptId] = it
            }
        }

        return evalSha(sha1, *keys, arg = emptyList())
    }

    @Test
    suspend fun `script operations test case`() {
        val mainJob = Job()
        val coroutineScope = CoroutineScope(
            Dispatchers.IO + CoroutineName("CustomCoroutineScope") + mainJob,
        )

        val map1 = ConcurrentMap<Int, Job>()
        val map2 = ConcurrentMap<Int, Job>()

        val totalRepeats = 1_000
        repeat(totalRepeats) { id ->
            client.fastEval(
                "script1",
                """
                    redis.call('HSET', KEYS[1], ARGV[2], ARGV[3], ARGV[4], ARGV[5], ARGV[6], ARGV[7])
                    redis.call('HPEXPIRE', KEYS[1], 60000, 'FIELDS', 1, ARGV[2])
                    redis.call('SADD', KEYS[2],  ARGV[1])
                """.trimIndent(),
                "some:key:$id",
                "some:key",
                id.toString(),
                *mapOf(
                    "some:field1" to "some-value1-$id",
                    "some:field2" to "some-value2-$id",
                    "some:field3" to "some-value3-$id",
                ).flatMap { listOf(it.key, it.value) }.toTypedArray(),
            )

            map2[id] = coroutineScope
                .launch(start = CoroutineStart.LAZY) {
                    repeat(totalRepeats) {
                        client.hExpire("some:key:$id", 1.minutes, "some-value1-$id")
                        delay(2.seconds)
                    }
                }.also { job ->
                    job.invokeOnCompletion {
                        map2.remove(id)
                    }

                    job.start()
                }

            map1[id] = coroutineScope
                .launch(start = CoroutineStart.LAZY) {
                    delay(10.seconds)

                    eventually(1.seconds) {
                        client.hGet("some:key:$id", "some:field1") shouldBe "some-value1-$id"
                    }
                    eventually(1.seconds) {
                        client.hGet("some:key:$id", "some:field2") shouldBe "some-value2-$id"
                    }
                    eventually(1.seconds) {
                        client.hGet("some:key:$id", "some:field3") shouldBe "some-value3-$id"
                    }

                    client.fastEval(
                        "script2",
                        """
                        redis.call('DEL', KEYS[1])
                        redis.call('SREM', KEYS[2], ARGV[1])
                        """.trimIndent(),
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
        joinAll(*mainJob.children.toList().toTypedArray())
    }
}
