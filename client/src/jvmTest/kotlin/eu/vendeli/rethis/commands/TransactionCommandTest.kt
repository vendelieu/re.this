package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.codecs.string.SetCommandCodec
import eu.vendeli.rethis.codecs.transaction.DiscardCommandCodec
import eu.vendeli.rethis.codecs.transaction.ExecCommandCodec
import eu.vendeli.rethis.codecs.transaction.MultiCommandCodec
import eu.vendeli.rethis.codecs.transaction.UnwatchCommandCodec
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
import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.types.common.RConnection
import eu.vendeli.rethis.types.coroutine.CoLocalConn
import eu.vendeli.rethis.utils.execute
import eu.vendeli.rethis.utils.toRESPBuffer
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.withContext

class TransactionCommandTest : ReThisTestCtx() {
    // WATCH/MULTI state lives on the server-side connection, and a plain client call borrows a pooled connection per
    // command. A connection left WATCHing a key that is later modified keeps the dirty flag, makes the next WATCH on
    // it a no-op and the next EXEC return null. So run such tests on one pinned connection that is reset before use
    // (a leaked state from elsewhere) and after use (DISCARD + UNWATCH, so a failed assertion cannot leak it either).
    private suspend fun <T> withPinnedConnection(block: suspend (RConnection) -> T): T {
        val provider = connectionProvider()
        val conn = provider.borrowConnection()
        try {
            resetConnection(conn)
            return withContext(CoLocalConn(conn, isTx = false)) { block(conn) }
        } finally {
            // best effort: a dead connection must not mask the original assertion failure
            runCatching { resetConnection(conn) }
            provider.releaseConnection(conn)
        }
    }

    private suspend fun resetConnection(conn: RConnection) {
        conn.doRequest(DiscardCommandCodec.encode(Charsets.UTF_8).data).readResponseWrapped(Charsets.UTF_8)
        conn.doRequest(UnwatchCommandCodec.encode(Charsets.UTF_8).data).readResponseWrapped(Charsets.UTF_8)
    }

    @Test
    suspend fun `test EXEC command with multiple queued commands`() {
        val cProvider = connectionProvider()
        val conn = cProvider.borrowConnection()

        conn
            .doRequest(MultiCommandCodec.encode(Charsets.UTF_8).data)
            .readResponseWrapped(charset = Charsets.UTF_8)
            .isOk()
            .shouldBeTrue()

        conn
            .doRequest(
                SetCommandCodec.encode(Charsets.UTF_8, "test1", "testv1").data,
            ).readResponseWrapped(Charsets.UTF_8)
            .shouldBe(PlainString("QUEUED"))

        conn
            .doRequest(
                SetCommandCodec.encode(Charsets.UTF_8, "test2", "testv2").data,
            ).readResponseWrapped(Charsets.UTF_8)
            .shouldBe(PlainString("QUEUED"))

        conn
            .doRequest(
                ExecCommandCodec.encode(Charsets.UTF_8).data,
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
        withPinnedConnection { conn ->
            client.multi().shouldBeTrue()
            client.set("testKey1", "testVal1")
            client.set("testKey2", "testVal2")
            conn
                .doRequest(listOf("GETBIT").toRESPBuffer())
                .readResponseWrapped()
                .shouldBeTypeOf<RType.Error>()
                .exception
                .message shouldBe "ERR wrong number of arguments for 'getbit' command"
            val abort = shouldThrow<ReThisException> { client.exec() }
            abort.message.shouldNotBeNull() shouldStartWith "EXECABORT"
        }
    }

    @Test
    suspend fun `test transaction with queued commands that fail`() {
        shouldThrow<TransactionInvalidStateException> {
            client.transaction {
                set("testKey1", "testVal1")
                set("testKey2", "testVal2")
                set("testKey2", "testVal2")
                throw RuntimeException("test")
            }
        }
    }

    @Test
    suspend fun `test WATCH command with multiple keys`() {
        withPinnedConnection {
            client.watch("watchKey1", "watchKey2")
            client.multi()
            client.set("watchKey1", "testVal1")
            client.set("watchKey2", "testVal2")
            client.exec() shouldBe listOf(PlainString("OK"), PlainString("OK"))
        }
    }

    @Test
    suspend fun `test UNWATCH command after WATCH command`() {
        val other = createClient()
        try {
            withPinnedConnection {
                client.watch("unwatchKey1", "unwatchKey2")
                // modifying a watched key from another connection would abort the transaction...
                other.set("unwatchKey1", "changed")
                // ...unless the keys were unwatched first
                client.unwatch()
                client.multi()
                client.set("unwatchKey1", "testVal1")
                client.set("unwatchKey2", "testVal2")
                client.exec() shouldBe listOf(PlainString("OK"), PlainString("OK"))
            }
        } finally {
            other.close()
        }
    }
}
