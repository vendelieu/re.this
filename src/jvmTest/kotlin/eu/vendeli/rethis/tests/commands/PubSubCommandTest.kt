package eu.vendeli.rethis.tests.commands

import eu.vendeli.rethis.ReThisException
import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.*
import eu.vendeli.rethis.exception
import eu.vendeli.rethis.types.common.PubSubNumEntry
import eu.vendeli.rethis.types.core.BulkString
import eu.vendeli.rethis.types.core.Int64
import eu.vendeli.rethis.types.core.Push
import eu.vendeli.rethis.types.core.SubscriptionEventHandler
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PubSubCommandTest : ReThisTestCtx() {
    @BeforeEach
    fun clearSubs() {
        client.subscriptions.unsubscribeAll()
    }

    @Test
    fun `test PUBLISH command`(): Unit = runTest {
        client.subscribe("testChannel") { _, _ -> println("test") }
        delay(100)
        client.publish("testChannel", "testMessage") shouldBe 1L
    }

    @Test
    fun `test PUBSUB CHANNELS command`(): Unit = runTest {
        client.subscribe("testChannel2") { _, _ -> println("test") }
        delay(100)
        client.pubSubChannels() shouldBe listOf("testChannel2")
    }

    @Test
    fun `test PUBSUB NUMPAT command`(): Unit = runTest {
        client.pSubscribe("testP*") { _, _ -> println("test") }
        delay(100)
        client.pubSubNumPat() shouldBe 1L
    }

    @Test
    fun `test PUBSUB NUMSUB command`(): Unit = runTest {
        client.pubSubNumSub("testChannel") shouldBe listOf(PubSubNumEntry("testChannel", 0))
    }

    @Test
    fun `test PUBSUB SHARDCHANNELS command`(): Unit = runTest {
        client.pubSubShardChannels() shouldBe emptyList()
    }

    @Test
    fun `test PUBSUB SHARDNUMSUB command`(): Unit = runTest {
        client.pubSubShardNumSub("testChannel") shouldBe listOf(PubSubNumEntry("testChannel", 0))
    }

    @Test
    fun `test PUNSUBSCRIBE command`(): Unit = runTest {
        client.pUnsubscribe("testPattern") shouldBe Push(
            listOf(
                BulkString("punsubscribe"),
                BulkString("testPattern"),
                Int64(0),
            ),
        )
    }

    @Test
    fun `test SPUBLISH command`(): Unit = runTest {
        client.sPublish("testShardChannel", "testMessage") shouldBe 0L
    }

    @Test
    fun `test SUNSUBSCRIBE command`(): Unit = runTest {
        client.sUnsubscribe("testPattern") shouldBe Push(
            listOf(
                BulkString("sunsubscribe"),
                BulkString("testPattern"),
                Int64(0),
            ),
        )
    }

    @Test
    fun `test UNSUBSCRIBE command`(): Unit = runTest {
        client.unsubscribe("testPattern") shouldBe Push(
            listOf(
                BulkString("unsubscribe"),
                BulkString("testPattern"),
                Int64(0),
            ),
        )
    }

    @Test
    fun `test PSUBSCRIBE command`(): Unit = runTest {
        client.pSubscribe("testPattern") { _, m ->
            println(m)
        }
        client.subscriptions.isActive("testPattern") shouldBe true
    }

    @Test
    fun `test SSUBSCRIBE command`(): Unit = runTest {
        client.sSubscribe("testShardChannel") { _, m ->
            println(m)
        }
        client.subscriptions.isActive("testShardChannel") shouldBe true
    }

    @Test
    fun `test SUBSCRIBE command`(): Unit = runTest {
        client.subscribe("testChannel") { _, m ->
            println(m)
        }
        client.subscriptions.isActive("testChannel") shouldBe true
    }

    @Test
    fun `test unsubscription command`(): Unit = runTest {
        client.subscribe("testChannel") { _, m ->
            println(m)
        }
        client.subscriptions.isActive("testChannel") shouldBe true

        client.subscriptions.unsubscribe("testChannel") shouldBe true
        client.subscriptions.isActive("testChannel") shouldBe false
        client.subscriptions.size shouldBe 0
    }

    @Test
    fun `test subscription evenHandler`(): Unit = runTest {
        var onSub = 0
        var onUnsub = 0
        var caughtEx: Exception? = null

        client.subscriptions.setEventHandler(
            object : SubscriptionEventHandler {
                override suspend fun onSubscribe(id: String, subscribedChannels: Long) {
                    println("-- id $id count: $subscribedChannels")
                    onSub++
                }

                override suspend fun onUnsubscribe(id: String, subscribedChannels: Long) {
                    println("!-- id $id count: $subscribedChannels")
                    onUnsub++
                }

                override suspend fun onException(id: String, ex: Exception) {
                    caughtEx = ex
                }
            },
        )
        client.subscribe("testChannel") { _, m ->
            println("-----------$m")
        }
        client.subscriptions.isActive("testChannel") shouldBe true

        client.pSubscribe("testCh*") { _, m ->
            exception { "test" }
        }
        client.subscriptions.isActive("testCh*") shouldBe true

        client.unsubscribe("testChannel")
        client.subscriptions.isActive("testChannel") shouldBe false
        client.publish("testChannel", "test")

        delay(100)

        onSub shouldBeGreaterThan 0
        onUnsub shouldBe 0
        caughtEx.shouldNotBeNull().shouldBeTypeOf<ReThisException>().shouldHaveMessage("test")
    }
}
