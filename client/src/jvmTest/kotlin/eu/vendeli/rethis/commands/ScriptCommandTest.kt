package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.codecs.scripting.FunctionFlushCommandCodec
import eu.vendeli.rethis.codecs.scripting.FunctionKillCommandCodec
import eu.vendeli.rethis.codecs.scripting.FunctionStatsCommandCodec
import eu.vendeli.rethis.codecs.scripting.ScriptKillCommandCodec
import eu.vendeli.rethis.command.scripting.*
import eu.vendeli.rethis.shared.request.common.FlushType
import eu.vendeli.rethis.shared.request.scripting.ScriptDebugMode
import eu.vendeli.rethis.shared.types.BulkString
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.PlainString
import eu.vendeli.rethis.shared.types.RArray
import eu.vendeli.rethis.shared.types.ReThisException
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.utils.readResponseWrapped
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeTypeOf
import eu.vendeli.rethis.types.common.RConnection
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.io.readString
import kotlin.time.Duration.Companion.seconds

// Redis only serves other clients (including KILL) once a script runs past busy-reply-threshold (5s by default).
private val KILL_TIMEOUT = 20.seconds

class ScriptCommandTest : ReThisTestCtx() {
    // The endless script runs on its own client; KILL is sent over one connection borrowed up front. A server error
    // reply does not drop the pooled connection, but a busy server cannot complete the handshake of a new one, so
    // KILL must not depend on opening a connection while the script runs; the retries also avoid the throwing client API.
    // KILL answers NOTBUSY until the script has reached the server: retry on that error only.
    private suspend fun runBlockingScript(kill: CommandRequest, block: suspend ReThis.() -> Unit) {
        val provider = connectionProvider()
        var killConn: RConnection? = null
        var scriptClient: ReThis? = null
        var running: Deferred<Result<Unit>>? = null
        try {
            val conn = provider.borrowConnection().also { killConn = it }
            val sc = createClient().also { scriptClient = it }
            running = sc.scope.async { runCatching { sc.block() } }
            eventually(KILL_TIMEOUT) {
                val reply = conn.doRequest(kill.data).readResponseWrapped(Charsets.UTF_8)
                if (reply is RType.Error) reply.exception.message.shouldNotBeNull() shouldStartWith "NOTBUSY"
                reply shouldBe PlainString("OK")
            }
            assertNoRunningScript(conn)
        } finally {
            killConn?.let { conn ->
                // best effort: never leave an endless script running on the shared server if the kill above failed
                runCatching { conn.doRequest(kill.data).readResponseWrapped(Charsets.UTF_8) }
            }
            running?.cancelAndJoin()
            scriptClient?.close()
            killConn?.let { provider.releaseConnection(it) }
        }
    }

    // Goes through the already borrowed connection: a pooled one could need a new handshake on a busy server.
    private suspend fun assertNoRunningScript(conn: RConnection) {
        eventually(KILL_TIMEOUT) {
            val stats = FunctionStatsCommandCodec.decode(
                conn.doRequest(FunctionStatsCommandCodec.encode(Charsets.UTF_8).data),
                Charsets.UTF_8,
            )
            stats["running_script"] shouldBe RType.Null
        }
    }

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
    suspend fun `test FUNCTION FLUSH command without mode`() {
        client.functionFlush(null) shouldBe true
    }

    @Test
    suspend fun `test FUNCTION FLUSH encodes no mode token when mode is null`() {
        FunctionFlushCommandCodec.encode(Charsets.UTF_8, null).data.readString() shouldBe
            "*2\r\n\$8\r\nFUNCTION\r\n\$5\r\nFLUSH\r\n"
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
        runBlockingScript(
            kill = FunctionKillCommandCodec.encode(Charsets.UTF_8),
            block = { fcall("fun10", arg = emptyList()) },
        )
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
        runBlockingScript(
            kill = ScriptKillCommandCodec.encode(Charsets.UTF_8),
            block = { eval("local count = 0; while true do count = count + 1; end", "counter", arg = emptyList()) },
        )
    }

    @Test
    suspend fun `SCRIPT KILL without a running script surfaces NOTBUSY`() {
        shouldThrow<ReThisException> { client.scriptKill() }.message.shouldNotBeNull() shouldStartWith "NOTBUSY"
    }

    @Test
    suspend fun `FUNCTION KILL without a running function surfaces NOTBUSY`() {
        shouldThrow<ReThisException> { client.functionKill() }.message.shouldNotBeNull() shouldStartWith "NOTBUSY"
    }

    @Test
    suspend fun `test SCRIPT LOAD command`() {
        val script = "return {KEYS[1],ARGV[1]}"
        client.scriptLoad(script) shouldBe "bfbf458525d6a0b19200bfd6db3af481156b367b"
    }
}
