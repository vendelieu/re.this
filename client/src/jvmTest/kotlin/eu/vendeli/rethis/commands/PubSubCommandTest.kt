package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.api.spec.common.response.common.PubSubNumEntry
import eu.vendeli.rethis.api.spec.common.types.*
import eu.vendeli.rethis.command.pubsub.*
import eu.vendeli.rethis.types.interfaces.SubscriptionEventHandler
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.delay

class PubSubCommandTest : ReThisTestCtx() {
    @BeforeEach
    fun clearSubs() {
        client.subscriptions.unsubscribeAll()
    }

    @Test
    suspend fun `test PUBLISH command`() {
        client.subscribe("testChannel") { _, _ -> println("test") }
        delay(100)
        client.publish("testChannel", "testMessage") shouldBe 1L
    }

    @Test
    suspend fun `test PUBSUB CHANNELS command`() {
        client.subscribe("testChannel2") { _, _ -> println("test") }
        delay(200)
        client.pubSubChannels().shouldNotBeEmpty()
    }

    @Test
    suspend fun `test PUBSUB NUMPAT command`() {
        client.pSubscribe("testP*") { _, _ -> println("test") }
        delay(200)
        client.pubSubNumPat() shouldBeGreaterThan 0
    }

    @Test
    suspend fun `test PUBSUB NUMSUB command`() {
        client.pubSubNumSub("testChannel") shouldBe listOf(PubSubNumEntry("testChannel", 1))
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
        client.subscriptions.subscriptionsHandlers["testPattern"].shouldNotBeNull()
    }

    @Test
    suspend fun `test SSUBSCRIBE command`() {
        client.sSubscribe("testShardChannel") { _, m ->
            println(m)
        }
        client.subscriptions.subscriptionsHandlers["testShardChannel"].shouldNotBeNull()
    }

    @Test
    suspend fun `test SUBSCRIBE command`() {
        client.subscribe("testChannel") { _, m ->
            println(m)
        }
        client.subscriptions.subscriptionsHandlers["testChannel"].shouldNotBeNull()
    }

    @Test
    suspend fun `test unsubscription command`() {
        client.subscribe("testChannel") { _, m ->
            println(m)
        }
        client.subscriptions.subscriptionsHandlers["testChannel"].shouldNotBeNull()

        client.subscriptions.unsubscribe("testChannel") shouldBe true
        client.subscriptions.subscriptionsHandlers["testChannel"].shouldBeNull()
        client.subscriptions.size shouldBe 0
    }

    @Test
    suspend fun `test subscription evenHandler`() {
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
        client.subscriptions.subscriptionsHandlers["testChannel"].shouldNotBeNull()

        client.pSubscribe("testCh*") { _, m ->
            processingException { "test" }
        }
        client.subscriptions.subscriptionsHandlers["testCh*"].shouldNotBeNull()

        client.unsubscribe("testChannel")
        client.subscriptions.subscriptionsHandlers["testChannel"].shouldBeNull()
        client.publish("testChannel", "test")

        delay(100)

        onSub shouldBeGreaterThan 0
        onUnsub shouldBe 0
        caughtEx.shouldNotBeNull().shouldBeTypeOf<DataProcessingException>() shouldHaveMessage ("test")
    }
}
