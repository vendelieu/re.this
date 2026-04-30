package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.pubsub.*
import eu.vendeli.rethis.shared.response.common.PubSubNumEntry
import eu.vendeli.rethis.shared.types.DataProcessingException
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.processingException
import eu.vendeli.rethis.types.common.PubSubKind
import eu.vendeli.rethis.types.common.SubscribeTarget
import eu.vendeli.rethis.types.interfaces.PubSubHandler
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class PubSubCommandTest : ReThisTestCtx() {
    @BeforeEach
    fun clearSubs() {
        client.subscriptions.unsubscribeAll()
    }

    @Test
    suspend fun `test PUBLISH command`() {
        client.subscribe("testChannel") { _, _: String -> }
        eventually(1.seconds) {
            client.publish("testChannel", "testMessage") shouldBe 1L
        }
    }

    @Test
    suspend fun `test PUBSUB CHANNELS command`() {
        client.subscribe("testChannel2") { _, _: String -> }
        eventually(1.seconds) {
            client.pubSubChannels().shouldNotBeEmpty()
        }
    }

    @Test
    suspend fun `test PUBSUB NUMPAT command`() {
        client.pSubscribe("testP*") { _, _: String -> }
        eventually(1.seconds) {
            client.pubSubNumPat() shouldBeGreaterThan 0
        }
    }

    @Test
    suspend fun `test PUBSUB NUMSUB command`() {
        client.subscribe("testChannel") { _, _: String -> }
        eventually(1.seconds) {
            client.pubSubNumSub("testChannel") shouldBe listOf(PubSubNumEntry("testChannel", 1))
        }
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
    suspend fun `test PUNSUBSCRIBE command`() = shouldNotThrowAny {
        client.pUnsubscribe("testPattern")
    }

    @Test
    suspend fun `test SPUBLISH command`() {
        client.sPublish("testShardChannel", "testMessage") shouldBe 0L
    }

    @Test
    suspend fun `test SUNSUBSCRIBE command`() = shouldNotThrowAny {
        client.sUnsubscribe("testPattern")
    }

    @Test
    suspend fun `test UNSUBSCRIBE command`() = shouldNotThrowAny {
        client.unsubscribe("testPattern")
    }

    @Test
    suspend fun `test PSUBSCRIBE command`() {
        client.pSubscribe("testPattern") { _, _: String -> }
        eventually(1.seconds) {
            client.subscriptions.isActiveHandlers(SubscribeTarget.Pattern("testPattern")).shouldBeTrue()
        }
    }

    @Test
    suspend fun `test SSUBSCRIBE command`() {
        client.sSubscribe("testShardChannel") { _, _: String -> }
        eventually(1.seconds) {
            client.subscriptions.isActiveHandlers(SubscribeTarget.Shard("testShardChannel")).shouldBeTrue()
        }
    }

    @Test
    suspend fun `test SUBSCRIBE command`() {
        client.subscribe("testChannel") { _, _: String -> }
        eventually(1.seconds) {
            client.subscriptions.isActiveHandlers(SubscribeTarget.Channel("testChannel")).shouldBeTrue()
        }
    }

    @Test
    suspend fun `test unsubscription command`() {
        client.subscribe("testChannel") { _, _: String -> }
        eventually(1.seconds) {
            client.subscriptions.isActiveHandlers(SubscribeTarget.Channel("testChannel")).shouldBeTrue()
        }

        client.subscriptions.unsubscribe(SubscribeTarget.Channel("testChannel"))
        eventually(1.seconds) {
            client.subscriptions.isActiveHandlers(SubscribeTarget.Channel("testChannel")).shouldBe(false)
        }
        client.subscriptions.size shouldBe 0
    }

    @Test
    suspend fun `test subscription eventHandler`() {
        val handlerRan = AtomicBoolean(false)
        val onMessage = AtomicInteger(0)
        val onUnsub = AtomicInteger(0)
        val caughtEx = AtomicReference<Exception?>(null)

        client.subscriptions.registerGlobalHandler(
            object : PubSubHandler {
                override suspend fun onSubscribe(kind: PubSubKind, target: SubscribeTarget, subscribedChannels: Long) {}
                override suspend fun onUnsubscribe(
                    kind: PubSubKind,
                    target: SubscribeTarget,
                    subscribedChannels: Long,
                ) {
                    onUnsub.incrementAndGet()
                }
                override suspend fun onMessage(kind: PubSubKind, channel: String, message: RType, pattern: String?) {}
                override suspend fun onException(target: SubscribeTarget, ex: Exception) {
                    caughtEx.set(ex)
                }
            },
        )

        val channelName = "eventHandlerChannel"
        client.subscribe(
            channelName,
            callback = object : PubSubHandler {
                override suspend fun onSubscribe(kind: PubSubKind, target: SubscribeTarget, subscribedChannels: Long) {}
                override suspend fun onUnsubscribe(
                    kind: PubSubKind,
                    target: SubscribeTarget,
                    subscribedChannels: Long,
                ) {}
                override suspend fun onMessage(kind: PubSubKind, channel: String, message: RType, pattern: String?) {
                    handlerRan.set(true)
                    onMessage.incrementAndGet()
                    processingException { "test" }
                }
                override suspend fun onException(target: SubscribeTarget, ex: Exception) {}
            },
        )

        eventually(1.seconds) {
            client.publish(channelName, "test") shouldBe 1L
        }
        eventually(2.seconds) {
            handlerRan.get() shouldBe true
            onMessage.get() shouldBeGreaterThan 0
            caughtEx
                .get()
                .shouldNotBeNull()
                .shouldBeTypeOf<DataProcessingException>()
                .shouldHaveMessage("test")
        }
        onUnsub.get() shouldBe 0
    }
}
