package eu.vendeli.rethis.tests.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.*
import eu.vendeli.rethis.types.core.BulkString
import eu.vendeli.rethis.types.core.RArray
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScriptCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test EVAL command`() {
        val script = "return {KEYS[1],ARGV[1]}"
        client.eval(script, 1, "testKey1", "testValue1") shouldBe
            RArray(listOf(BulkString("testKey1"), BulkString("testValue1")))
    }

    @Test
    suspend fun `test EVAL_RO command`() {
        val script = "return {KEYS[1],ARGV[1]}"
        client.evalRo(script, 1, "testKey2", "testValue2") shouldBe
            RArray(listOf(BulkString("testKey2"), BulkString("testValue2")))
    }

    @Test
    suspend fun `test EVALSHA command`() {
        val script = "return {KEYS[1],ARGV[1]}"
        val sha1 = client.scriptLoad(script).shouldNotBeNull()
        client.evalSha(sha1, 1, "testKey3", "testValue3") shouldBe
            RArray(listOf(BulkString("testKey3"), BulkString("testValue3")))
    }

    @Test
    suspend fun `test EVALSHA_RO command`() {
        val script = "return {KEYS[1],ARGV[1]}"
        val sha1 = client.scriptLoad(script).shouldNotBeNull()
        client.evalShaRo(sha1, 1, "testKey4", "testValue4") shouldBe
            RArray(listOf(BulkString("testKey4"), BulkString("testValue4")))
    }

    @Test
    suspend fun `test FCALL command`() {
        val script = "#!lua name=mylib\n redis.register_function('myfunc', function(keys, args) return args[1] end)"
        val name = "myfunc"
        client.functionLoad(script).shouldNotBeNull()
        client.fcall(name, 1, "testKey5", "testValue5") shouldBe BulkString("testValue5")
    }

    @Test
    suspend fun `test FCALL_RO command`() {
        val script =
            "#!lua name=mylib2\nlocal function myfunc2(keys, args) return args[1] end\nredis.register_function { function_name='myfunc2', callback=myfunc2, flags={ 'no-writes' }}"
        client.functionLoad(script).shouldNotBeNull()
        client.fcallRo("myfunc2", 1, "testKey6", "testValue6") shouldBe BulkString("testValue6")
    }

    @Test
    suspend fun `test FUNCTION DELETE command`() {
        val script = "#!lua name=mylib3\n redis.register_function('myfunc3', function(keys, args) return args[1] end)"
        client.functionLoad(script).shouldNotBeNull()
        client.functionDelete("mylib3") shouldBe "OK"
    }

    @Test
    suspend fun `test FUNCTION DUMP command`() {
        val script = "#!lua name=mylib4\n redis.register_function('myfunc4', function(keys, args) return args[1] end)"
        client.functionLoad(script).shouldNotBeNull()
        client
            .functionDump()
            .shouldNotBeNull()
            .toString(Charsets.UTF_8)
            .shouldContain("mylib4")
    }

    @Test
    suspend fun `test FUNCTION FLUSH command`() {
        client.functionFlush() shouldBe "OK"
    }

    @Test
    suspend fun `test FUNCTION KILL command`() {
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
        client.coScope.launch {
            client.fcall("fun10", 0)
        }
        delay(100)
        client.functionKill() shouldBe "OK"
    }

    @Test
    suspend fun `test FUNCTION LIST command`() {
        shouldNotThrowAny {
            client.functionList("mylib")
        }
    }

    @Test
    suspend fun `test FUNCTION LOAD command`() {
        val script = "#!lua name=mylib5\n redis.register_function('myfunc5', function(keys, args) return args[1] end)"
        client.functionLoad(script) shouldBe "mylib5"
    }

    @Test
    suspend fun `test FUNCTION RESTORE command`() {
        val script = "#!lua name=mylib11\n redis.register_function('myfunc11', function(keys, args) return args[1] end)"
        client.functionLoad(script).shouldNotBeNull()
        val dump = client.functionDump().shouldNotBeNull()
        client.functionFlush()
        delay(100)
        client.functionRestore(dump) shouldBe "OK"
    }

    @Test
    suspend fun `test FUNCTION STATS command`() {
        val script = "#!lua name=mylib6\n redis.register_function('myfunc6', function(keys, args) return args[1] end)"
        client.functionLoad(script).shouldNotBeNull()
        client.functionStats().shouldNotBeNull().containsKey("running_script")
    }

    @Test
    suspend fun `test SCRIPT DEBUG command`() {
        client.scriptDebug("SYNC") shouldBe "OK"
    }

    @Test
    suspend fun `test SCRIPT EXISTS command`() {
        val script = "return {KEYS[1],ARGV[1]}"
        val sha1 = client.scriptLoad(script).shouldNotBeNull()
        client.scriptExists(sha1) shouldBe listOf(true)
    }

    @Test
    suspend fun `test SCRIPT FLUSH command`() {
        client.scriptFlush() shouldBe "OK"
    }

    @Test
    suspend fun `test SCRIPT KILL command`() {
        client.coScope.launch {
            client
                .eval(
                    "local count = 0; while true do count = count + 1; end",
                    1,
                    "counter",
                ).shouldNotBeNull()
        }
        delay(100)
        client.scriptKill() shouldBe true
    }

    @Test
    suspend fun `test SCRIPT LOAD command`() {
        val script = "return {KEYS[1],ARGV[1]}"
        client.scriptLoad(script) shouldBe "bfbf458525d6a0b19200bfd6db3af481156b367b"
    }
}
