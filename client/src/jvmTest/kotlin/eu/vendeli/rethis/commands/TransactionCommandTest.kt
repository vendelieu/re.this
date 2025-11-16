package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.codecs.string.SetCommandCodec
import eu.vendeli.rethis.codecs.transaction.ExecCommandCodec
import eu.vendeli.rethis.codecs.transaction.MultiCommandCodec
import eu.vendeli.rethis.command.generic.del
import eu.vendeli.rethis.command.json.jsonClear
import eu.vendeli.rethis.command.string.set
import eu.vendeli.rethis.command.transaction.exec
import eu.vendeli.rethis.command.transaction.multi
import eu.vendeli.rethis.command.transaction.unwatch
import eu.vendeli.rethis.command.transaction.watch
import eu.vendeli.rethis.shared.types.*
import eu.vendeli.rethis.shared.utils.isOk
import eu.vendeli.rethis.shared.utils.readResponseWrapped
import eu.vendeli.rethis.types.coroutine.CoLocalConn
import eu.vendeli.rethis.utils.execute
import eu.vendeli.rethis.utils.toRESPBuffer
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.launch

class TransactionCommandTest : ReThisTestCtx() {
    @BeforeAll
    fun prepare() = rewriteCfg {
        retry { times = 1 }
    }

    @Test
    suspend fun `test EXEC command with multiple queued commands`() {
        val cProvider = connectionProvider()
        val conn = cProvider.borrowConnection()

        conn
            .doRequest(MultiCommandCodec.encode(Charsets.UTF_8).buffer)
            .readResponseWrapped(charset = Charsets.UTF_8)
            .isOk()
            .shouldBeTrue()

        conn
            .doRequest(
                SetCommandCodec.encode(Charsets.UTF_8, "test1", "testv1").buffer,
            ).readResponseWrapped(Charsets.UTF_8)
            .shouldBe(PlainString("QUEUED"))

        conn
            .doRequest(
                SetCommandCodec.encode(Charsets.UTF_8, "test2", "testv2").buffer,
            ).readResponseWrapped(Charsets.UTF_8)
            .shouldBe(PlainString("QUEUED"))

        conn
            .doRequest(
                ExecCommandCodec.encode(Charsets.UTF_8).buffer,
            ).readResponseWrapped(Charsets.UTF_8)
            .shouldBeTypeOf<RArray>() shouldBe RArray(
            listOf(
                PlainString("OK"),
                PlainString("OK"),
            ),
        )
        cProvider.releaseConnection(conn)
    }

    @Test
    suspend fun `test transaction util`() {
        client.transaction {
            client.set("testKey1", "testVal1")
            set("testKey2", "testVal2")
            set("testKey3", "testVal3")
            del("testKey1")
        } shouldBe listOf(PlainString("OK"), PlainString("OK"), PlainString("OK"), Int64(1))
    }

    @Test
    suspend fun `test EXEC command with queued commands that fail`() {
        val provider = connectionProvider()
        val conn = provider.borrowConnection()
        client
            .scope
            .launch(CoLocalConn(conn)) {
                client.multi()
                client.set("testKey1", "testVal1")
                client.set("testKey2", "testVal2")
                client
                    .execute(listOf("GETBIT").toRESPBuffer())
                    .readResponseWrapped()
                    .shouldBeTypeOf<RType.Error>()
                    .exception
                    .message shouldBe "ERR wrong number of arguments for 'getbit' command"
                client.exec()
            }.join()
        provider.releaseConnection(conn)
    }

    @Test
    suspend fun `test transaction with queued commands that fail`() = shouldThrow<ReThisException> {
        client.transaction {
            set("testKey1", "testVal1")
            set("testKey2", "testVal2")
            set("testKey2", "testVal2")
            jsonClear("test")
        }
    }.cause.shouldNotBeNull().message shouldBe "ERR unknown command 'JSON.CLEAR', with args beginning with: 'test' "

    @Test
    suspend fun `test WATCH command with multiple keys`() {
        val provider = connectionProvider()
        val conn = provider.borrowConnection()
        client
            .scope
            .launch(CoLocalConn(conn)) {
                client.watch("testKey1", "testKey2")
                client.set("testKey1", "testVal1")
                client.set("testKey2", "testVal2")
                client.watch("testKey1", "testKey2")
                client.set("testKey1", "testVal1")
                client.set("testKey2", "testVal2")
            }.join()
        provider.releaseConnection(conn)
    }

    @Test
    suspend fun `test UNWATCH command after WATCH command`() {
        shouldNotThrowAny {
            client.watch("testKey1", "testKey2")
            client.unwatch()
            client.set("testKey1", "testVal1")
            client.set("testKey2", "testVal2")
        }
    }
}
