package eu.vendeli.rethis.tests.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.*
import eu.vendeli.rethis.types.core.BulkString
import eu.vendeli.rethis.types.core.RArray
import eu.vendeli.rethis.utils.coLaunch
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.utils.io.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ScriptCommandTest : ReThisTestCtx() {
    @Test
    fun `test EVAL command`(): Unit = runTest {
        val script = "return {KEYS[1],ARGV[1]}"
        client.eval(script, 1, "testKey1", "testValue1") shouldBe
            RArray(listOf(BulkString("testKey1"), BulkString("testValue1")))
    }

    @Test
    fun `test EVAL_RO command`(): Unit = runTest {
        val script = "return {KEYS[1],ARGV[1]}"
        client.evalRo(script, 1, "testKey2", "testValue2") shouldBe
            RArray(listOf(BulkString("testKey2"), BulkString("testValue2")))
    }

    @Test
    fun `test EVALSHA command`(): Unit = runTest {
        val script = "return {KEYS[1],ARGV[1]}"
        val sha1 = client.scriptLoad(script).shouldNotBeNull()
        client.evalSha(sha1, 1, "testKey3", "testValue3") shouldBe
            RArray(listOf(BulkString("testKey3"), BulkString("testValue3")))
    }

    @Test
    fun `test EVALSHA_RO command`(): Unit = runTest {
        val script = "return {KEYS[1],ARGV[1]}"
        val sha1 = client.scriptLoad(script).shouldNotBeNull()
        client.evalShaRo(sha1, 1, "testKey4", "testValue4") shouldBe
            RArray(listOf(BulkString("testKey4"), BulkString("testValue4")))
    }

    @Test
    fun `test FCALL command`(): Unit = runTest {
        val script = "#!lua name=mylib\n redis.register_function('myfunc', function(keys, args) return args[1] end)"
        val name = "myfunc"
        client.functionLoad(script).shouldNotBeNull()
        client.fcall(name, 1, "testKey5", "testValue5") shouldBe BulkString("testValue5")
    }

    @Test
    fun `test FCALL_RO command`(): Unit = runTest {
        val script =
            "#!lua name=mylib2\nlocal function myfunc2(keys, args) return args[1] end\nredis.register_function { function_name='myfunc2', callback=myfunc2, flags={ 'no-writes' }}"
        client.functionLoad(script).shouldNotBeNull()
        client.fcallRo("myfunc2", 1, "testKey6", "testValue6") shouldBe BulkString("testValue6")
    }

    @Test
    fun `test FUNCTION DELETE command`(): Unit = runTest {
        val script = "#!lua name=mylib3\n redis.register_function('myfunc3', function(keys, args) return args[1] end)"
        client.functionLoad(script).shouldNotBeNull()
        client.functionDelete("mylib3") shouldBe "OK"
    }

    @Test
    fun `test FUNCTION DUMP command`(): Unit = runTest {
        val script = "#!lua name=mylib4\n redis.register_function('myfunc4', function(keys, args) return args[1] end)"
        client.functionLoad(script).shouldNotBeNull()
        client
            .functionDump()
            .shouldNotBeNull()
            .toString(Charsets.UTF_8)
            .shouldContain("mylib4")
    }

    @Test
    fun `test FUNCTION FLUSH command`(): Unit = runTest {
        client.functionFlush() shouldBe "OK"
    }

    @Test
    fun `test FUNCTION KILL command`(): Unit = runTest {
        val script =
            "#!lua name=mylib10\n" +
                "local function myfunc10(keys, args)\n" +
                "    local count = 0\n" +
                "    while true do\n" +
                "        count = count + 1\n" +
                "    end\n" +
                "    return count\n" +
                "end\n" +
                "\n" +
                "redis.register_function { function_name='fun10', callback=myfunc10, flags={ 'no-writes' } }"
        client.functionLoad(script).shouldNotBeNull()
        client.coLaunch {
            client.fcall("fun10", 0)
        }
        delay(100)
        client.functionKill() shouldBe "OK"
    }

    @Test
    fun `test FUNCTION LIST command`(): Unit = runTest {
        shouldNotThrowAny {
            client.functionList("mylib")
        }
    }

    @Test
    fun `test FUNCTION LOAD command`(): Unit = runTest {
        val script = "#!lua name=mylib5\n redis.register_function('myfunc5', function(keys, args) return args[1] end)"
        client.functionLoad(script) shouldBe "mylib5"
    }

    @Test
    fun `test FUNCTION RESTORE command`(): Unit = runTest {
        val script = "#!lua name=mylib11\n redis.register_function('myfunc11', function(keys, args) return args[1] end)"
        client.functionLoad(script).shouldNotBeNull()
        val dump = client.functionDump().shouldNotBeNull()
        client.functionFlush()
        delay(100)
        client.functionRestore(dump) shouldBe "OK"
    }

    @Test
    fun `test FUNCTION STATS command`(): Unit = runTest {
        val script = "#!lua name=mylib6\n redis.register_function('myfunc6', function(keys, args) return args[1] end)"
        client.functionLoad(script).shouldNotBeNull()
        client.functionStats().shouldNotBeNull().containsKey("running_script")
    }

    @Test
    fun `test SCRIPT DEBUG command`(): Unit = runTest {
        client.scriptDebug("SYNC") shouldBe "OK"
    }

    @Test
    fun `test SCRIPT EXISTS command`(): Unit = runTest {
        val script = "return {KEYS[1],ARGV[1]}"
        val sha1 = client.scriptLoad(script).shouldNotBeNull()
        client.scriptExists(sha1) shouldBe listOf(true)
    }

    @Test
    fun `test SCRIPT FLUSH command`(): Unit = runTest {
        client.scriptFlush() shouldBe "OK"
    }

    @Test
    fun `test SCRIPT KILL command`(): Unit = runTest {
        client.coLaunch {
            client
                .eval(
                    "local count = 0; while true do count = count + 1; end",
                    1,
                    "counter",
                ).shouldNotBeNull()
        }
        delay(100)
        client.scriptKill() shouldBe "OK"
    }

    @Test
    fun `test SCRIPT LOAD command`(): Unit = runTest {
        val script = "return {KEYS[1],ARGV[1]}"
        client.scriptLoad(script) shouldBe "bfbf458525d6a0b19200bfd6db3af481156b367b"
    }
}
