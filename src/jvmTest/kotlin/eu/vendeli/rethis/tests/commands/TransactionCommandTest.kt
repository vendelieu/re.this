package eu.vendeli.rethis.tests.commands

import eu.vendeli.rethis.ReThisException
import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.*
import eu.vendeli.rethis.types.core.PlainString
import eu.vendeli.rethis.types.core.RArray
import eu.vendeli.rethis.types.core.toArg
import eu.vendeli.rethis.types.coroutine.CoLocalConn
import eu.vendeli.rethis.utils.bufferValues
import eu.vendeli.rethis.utils.coLaunch
import eu.vendeli.rethis.utils.readRedisMessage
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class TransactionCommandTest : ReThisTestCtx() {
    @Test
    fun `test EXEC command with multiple queued commands`(): Unit = runTest {
        val conn = client.connectionPool.acquire()

        conn.output.writeBuffer(bufferValues(listOf("MULTI".toArg()), Charsets.UTF_8))
        conn.output.flush()
        conn.input.readRedisMessage(Charsets.UTF_8) shouldBe PlainString("OK")

        conn.output.writeBuffer(bufferValues(listOf("SET".toArg(), "test3".toArg(), "testv3".toArg()), Charsets.UTF_8))
        conn.output.flush()
        conn.input.readRedisMessage(Charsets.UTF_8) shouldBe PlainString("QUEUED")

        conn.output.writeBuffer(bufferValues(listOf("SET".toArg(), "test4".toArg(), "testv4".toArg()), Charsets.UTF_8))
        conn.output.flush()
        conn.input.readRedisMessage(Charsets.UTF_8) shouldBe PlainString("QUEUED")

        conn.output.writeBuffer(bufferValues(listOf("EXEC".toArg()), Charsets.UTF_8))
        conn.output.flush()
        conn.input.readRedisMessage(Charsets.UTF_8) shouldBe RArray(listOf(PlainString("OK"), PlainString("OK")))
    }

    @Test
    fun `test transaction util`(): Unit = runTest {
        client.transaction {
            client.set("testKey1", "testVal1")
            client.set("testKey2", "testVal2")
            client.set("testKey3", "testVal3")
        } shouldBe listOf(PlainString("OK"), PlainString("OK"), PlainString("OK"))
    }

    @Test
    fun `test EXEC command with queued commands that fail`(): Unit = runTest {
        val conn = client.connectionPool.acquire()
        client
            .coLaunch(currentCoroutineContext() + CoLocalConn(conn)) {
                client.multi()
                client.set("testKey1", "testVal1")
                client.set("testKey2", "testVal2")
                conn.output.writeBuffer(bufferValues(listOf("get".toArg()), Charsets.UTF_8))
                conn.output.flush()
                shouldThrow<ReThisException> { client.exec() }.message shouldBe
                    "ERR wrong number of arguments for 'get' command"
            }.join()
        client.connectionPool.release(conn)
    }

    @Test
    fun `test transaction with queued commands that fail`(): Unit = runTest {
        client.transaction {
            set("testKey1", "testVal1")
            set("testKey2", "testVal2")
            set("testKey2", "testVal2")
            jsonClear("test")
        } shouldHaveSize 0
    }

    @Test
    fun `test WATCH command with multiple keys`(): Unit = runTest {
        val conn = client.connectionPool.acquire()
        client
            .coLaunch(currentCoroutineContext() + CoLocalConn(conn)) {
                client.watch("testKey1", "testKey2")
                client.set("testKey1", "testVal1")
                client.set("testKey2", "testVal2")
                client.watch("testKey1", "testKey2")
                client.set("testKey1", "testVal1")
                client.set("testKey2", "testVal2")
            }.join()
        client.connectionPool.release(conn)
    }

    @Test
    fun `test UNWATCH command after WATCH command`(): Unit = runTest {
        shouldNotThrowAny {
            client.watch("testKey1", "testKey2")
            client.unwatch()
            client.set("testKey1", "testVal1")
            client.set("testKey2", "testVal2")
        }
    }
}
