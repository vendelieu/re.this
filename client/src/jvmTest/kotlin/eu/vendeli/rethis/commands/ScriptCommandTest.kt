package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.scripting.*
import eu.vendeli.rethis.shared.request.common.FlushType
import eu.vendeli.rethis.shared.request.scripting.ScriptDebugMode
import eu.vendeli.rethis.shared.types.BulkString
import eu.vendeli.rethis.shared.types.RArray
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.io.readString

class ScriptCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test EVAL command`() {
        val script = "return {KEYS[1],ARGV[1]}"
        client.eval(script, "testKey1", arg = listOf("testValue1")) shouldBe
            RArray(listOf(BulkString("testKey1"), BulkString("testValue1")))
    }

    @Test
    suspend fun `test EVAL_RO command`() {
        val script = "return {KEYS[1],ARGV[1]}"
        val result = client.evalRo(script, "testKey1", arg = listOf("testValue1"))
        result.shouldBeTypeOf<RArray>()
        result.value.size shouldBe 2
        result.value[0]
            .shouldBeTypeOf<BulkString>()
            .value
            .readString() shouldBe "testKey1"
        result.value[1]
            .shouldBeTypeOf<BulkString>()
            .value
            .readString() shouldBe "testValue1"
    }

    @Test
    suspend fun `test EVALSHA command`() {
        val script = "return {KEYS[1],ARGV[1]}"
        val sha1 = client.scriptLoad(script).shouldNotBeNull()
        val result = client.evalSha(sha1, "testKey3", arg = listOf("testValue3")).shouldBeTypeOf<RArray>()

        result.value.size shouldBe 2
        result.value[0]
            .shouldBeTypeOf<BulkString>()
            .value
            .readString() shouldBe "testKey3"
        result.value[1]
            .shouldBeTypeOf<BulkString>()
            .value
            .readString() shouldBe "testValue3"
    }

    @Test
    suspend fun `test EVALSHA_RO command`() {
        val script = "return {KEYS[1],ARGV[1]}"
        val sha1 = client.scriptLoad(script).shouldNotBeNull()

        val result = client.evalShaRo(sha1, "testKey4", arg = listOf("testValue4")).shouldBeTypeOf<RArray>()
        result.value.size shouldBe 2
        result.value[0]
            .shouldBeTypeOf<BulkString>()
            .value
            .readString() shouldBe "testKey4"
        result.value[1]
            .shouldBeTypeOf<BulkString>()
            .value
            .readString() shouldBe "testValue4"
    }

    @Test
    suspend fun `test FCALL command`() {
        val script = "#!lua name=mylib\n redis.register_function('myfunc', function(keys, args) return args[1] end)"
        val name = "myfunc"
        client.functionLoad(script).shouldNotBeNull()
        val result = client.fcall(name, "testKey5", arg = listOf("testValue5"))
        result.shouldBeTypeOf<BulkString>().value.readString() shouldBe "testValue5"
    }

    @Test
    suspend fun `test FCALL_RO command`() {
        val script =
            "#!lua name=mylib2\nlocal function myfunc2(keys, args) return args[1] end\nredis.register_function { function_name='myfunc2', callback=myfunc2, flags={ 'no-writes' }}"
        client.functionLoad(script).shouldNotBeNull()
        client.fcallRo("myfunc2", "testKey6", arg = listOf("testValue6")) shouldBe BulkString("testValue6")
    }

    @Test
    suspend fun `test FUNCTION DELETE command`() {
        val script = "#!lua name=mylib3\n redis.register_function('myfunc3', function(keys, args) return args[1] end)"
        client.functionLoad(script).shouldNotBeNull()
        client.functionDelete("mylib3") shouldBe true
    }

    @Test
    suspend fun `test FUNCTION DUMP command`() {
        val script = "#!lua name=mylib4\n redis.register_function('myfunc4', function(keys, args) return args[1] end)"
        client.functionLoad(script).shouldNotBeNull()
        client
            .functionDump()
            .shouldNotBeNull()
            .decodeToString()
            .shouldContain("mylib4")
    }

    @Test
    suspend fun `test FUNCTION FLUSH command`() {
        client.functionFlush(FlushType.SYNC) shouldBe true
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
        client.scope.launch {
            client.fcall("fun10", arg = emptyList())
        }
        delay(100)
        client.functionKill() shouldBe true
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
        client.functionFlush(FlushType.SYNC)
        delay(100)
        client.functionRestore(dump) shouldBe true
    }

    @Test
    suspend fun `test FUNCTION STATS command`() {
        val script = "#!lua name=mylib6\n redis.register_function('myfunc6', function(keys, args) return args[1] end)"
        client.functionLoad(script).shouldNotBeNull()
        client.functionStats().shouldNotBeNull().containsKey("running_script")
    }

    @Test
    suspend fun `test SCRIPT DEBUG command`() {
        client.scriptDebug(ScriptDebugMode.SYNC) shouldBe true
        client.scriptDebug(ScriptDebugMode.NO) shouldBe true
    }

    @Test
    suspend fun `test SCRIPT EXISTS command`() {
        val script = "return {KEYS[1],ARGV[1]}"
        val sha1 = client.scriptLoad(script).shouldNotBeNull()
        client.scriptExists(sha1) shouldBe listOf(true)
    }

    @Test
    suspend fun `test SCRIPT FLUSH command`() {
        client.scriptFlush() shouldBe true
    }

    @Test
    suspend fun `test SCRIPT KILL command`() {
        client.scope.launch {
            client
                .eval(
                    "local count = 0; while true do count = count + 1; end",
                    "counter",
                    arg = emptyList(),
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
