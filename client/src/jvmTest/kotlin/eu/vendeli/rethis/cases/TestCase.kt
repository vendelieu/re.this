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
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import java.util.concurrent.ConcurrentHashMap

private const val REPEAT_COUNT = 1_000
private val VERIFY_DELAY = 10.seconds
private val EXPIRE_INTERVAL = 2.seconds
private val EVENTUALLY_DURATION = 1.seconds
private val HASH_TTL = 1.minutes

private val HASH_SETUP_SCRIPT = """
    redis.call('HSET', KEYS[1], ARGV[2], ARGV[3], ARGV[4], ARGV[5], ARGV[6], ARGV[7])
    redis.call('HPEXPIRE', KEYS[1], 60000, 'FIELDS', 1, ARGV[2])
    redis.call('SADD', KEYS[2],  ARGV[1])
""".trimIndent()

private val HASH_TEARDOWN_SCRIPT = """
    redis.call('DEL', KEYS[1])
    redis.call('SREM', KEYS[2], ARGV[1])
""".trimIndent()

class TestCase : ReThisTestCtx() {
    private val scriptsSha1Map = ConcurrentHashMap<String, String>()
    private val scriptLoadMutex = Mutex()

    internal suspend fun ReThis.fastEval(
        scriptId: String,
        script: String,
        keys: Array<out String>,
        arg: List<String>,
    ): RType {
        val sha1 = scriptsSha1Map[scriptId] ?: scriptLoadMutex.withLock {
            scriptsSha1Map[scriptId] ?: scriptLoad(script).also {
                scriptsSha1Map[scriptId] = it
            }
        }
        return evalSha(sha1, *keys, arg = arg)
    }

    private suspend fun assertHashFields(client: ReThis, id: Int) {
        eventually(EVENTUALLY_DURATION) {
            client.hGet("some:key:$id", "some:field1") shouldBe "some-value1-$id"
        }
        eventually(EVENTUALLY_DURATION) {
            client.hGet("some:key:$id", "some:field2") shouldBe "some-value2-$id"
        }
        eventually(EVENTUALLY_DURATION) {
            client.hGet("some:key:$id", "some:field3") shouldBe "some-value3-$id"
        }
    }

    private suspend fun runConcurrentHashScenario(
        totalRepeats: Int,
        setup: suspend (id: Int) -> Unit,
        teardown: suspend (id: Int) -> Unit,
    ) {
        val mainJob = Job()
        val scope = CoroutineScope(Dispatchers.IO + CoroutineName("HashScenario") + mainJob)
        val allJobs = mutableListOf<Job>()

        repeat(totalRepeats) { id ->
            setup(id)

            val expireJob = scope.launch {
                repeat(totalRepeats) {
                    client.hExpire("some:key:$id", HASH_TTL, "some-value1-$id")
                    delay(EXPIRE_INTERVAL)
                }
            }
            allJobs.add(expireJob)

            val verifierJob = scope.launch {
                try {
                    delay(VERIFY_DELAY)
                    assertHashFields(client, id)
                    teardown(id)
                } finally {
                    expireJob.cancel()
                }
            }
            allJobs.add(verifierJob)
        }

        joinAll(*allJobs.toTypedArray())
    }

    @Test
    suspend fun `operations in transaction test case`() {
        runConcurrentHashScenario(
            REPEAT_COUNT,
            setup = { id ->
                client.transaction {
                    hSet(
                        "some:key:$id",
                        FieldValue("some:field1", "some-value1-$id"),
                        FieldValue("some:field2", "some-value2-$id"),
                        FieldValue("some:field3", "some-value3-$id"),
                    )
                    hExpire("some:key:$id", HASH_TTL, "some-value1-$id")
                    sAdd("some:key", id.toString())
                }
            },
            teardown = { id ->
                client.transaction {
                    del("some:key:$id")
                    sRem("some:key", id.toString())
                }
            },
        )
    }

    @Test
    suspend fun `script operations test case`() {
        runConcurrentHashScenario(
            REPEAT_COUNT,
            setup = { id ->
                client.fastEval(
                    "script1",
                    HASH_SETUP_SCRIPT,
                    keys = arrayOf("some:key:$id", "some:key"),
                    arg = listOf(
                        id.toString(),
                        "some:field1",
                        "some-value1-$id",
                        "some:field2",
                        "some-value2-$id",
                        "some:field3",
                        "some-value3-$id",
                    ),
                )
            },
            teardown = { id ->
                client.fastEval(
                    "script2",
                    HASH_TEARDOWN_SCRIPT,
                    keys = arrayOf("some:key:$id", "some:key"),
                    arg = listOf(id.toString()),
                )
            },
        )
    }
}
