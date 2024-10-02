package eu.vendeli.rethis.tests.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.*
import eu.vendeli.rethis.types.common.PubSubNumEntry
import eu.vendeli.rethis.types.core.BulkString
import eu.vendeli.rethis.types.core.Int64
import eu.vendeli.rethis.types.core.Push
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay

class PubSubCommandTest : ReThisTestCtx() {
    private suspend fun clearSubs() {
        client.subscriptions.entries.forEach {
            it.value.cancelAndJoin()
        }
    }

    @Test
    suspend fun `test PUBLISH command`() {
        client.subscribe("testChannel") { _, _ -> println("test") }
        delay(100)
        client.publish("testChannel", "testMessage") shouldBe 1L
        clearSubs()
    }

    @Test
    suspend fun `test PUBSUB CHANNELS command`() {
        client.subscribe("testChannel2") { _, _ -> println("test") }
        delay(100)
        client.pubSubChannels() shouldBe listOf("testChannel2")
        clearSubs()
    }

    @Test
    suspend fun `test PUBSUB NUMPAT command`() {
        client.pSubscribe("testP*") { _, _ -> println("test") }
        delay(100)
        client.pubSubNumPat() shouldBe 1L
        clearSubs()
    }

    @Test
    suspend fun `test PUBSUB NUMSUB command`() {
        client.pubSubNumSub("testChannel") shouldBe listOf(PubSubNumEntry("testChannel", 0))
    }

    @Test
    suspend fun `test PUBSUB SHARDCHANNELS command`() {
        client.pubSubShardChannels() shouldBe emptyList()
    }

    @Test
    suspend fun `test PUBSUB SHARDNUMSUB command`() {
        client.pubSubShardNumSub("testChannel") shouldBe listOf(PubSubNumEntry("testChannel", 0))
    }

    @Test
    suspend fun `test PUNSUBSCRIBE command`() {
        client.pUnsubscribe("testPattern") shouldBe Push(
            listOf(
                BulkString("punsubscribe"),
                BulkString("testPattern"),
                Int64(0),
            ),
        )
    }

    @Test
    suspend fun `test SPUBLISH command`() {
        client.sPublish("testShardChannel", "testMessage") shouldBe 0L
    }

    @Test
    suspend fun `test SUNSUBSCRIBE command`() {
        client.sUnsubscribe("testPattern") shouldBe Push(
            listOf(
                BulkString("sunsubscribe"),
                BulkString("testPattern"),
                Int64(0),
            ),
        )
    }

    @Test
    suspend fun `test UNSUBSCRIBE command`() {
        client.unsubscribe("testPattern") shouldBe Push(
            listOf(
                BulkString("unsubscribe"),
                BulkString("testPattern"),
                Int64(0),
            ),
        )
    }

    @Test
    suspend fun `test PSUBSCRIBE command`() {
        client.pSubscribe("testPattern") { _, m ->
            println(m)
        }
        client.subscriptions["testPattern"].shouldNotBeNull()
    }

    @Test
    suspend fun `test SSUBSCRIBE command`() {
        client.sSubscribe("testShardChannel") { _, m ->
            println(m)
        }
        client.subscriptions["testShardChannel"].shouldNotBeNull()
    }

    @Test
    suspend fun `test SUBSCRIBE command`() {
        client.subscribe("testChannel") { _, m ->
            println(m)
        }
        client.subscriptions["testChannel"].shouldNotBeNull()
    }
}
