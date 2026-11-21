package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.connection.ping
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

private val CLEAR_TIMEOUT = 5.seconds

class PubSubCommandTest : ReThisTestCtx() {
    @BeforeEach
    suspend fun clearSubs() {
        client.subscriptions.unsubscribeAll()
        // unsubscribeAll only fires the unsubscribe requests, so wait until the server has processed them
        eventually(CLEAR_TIMEOUT) {
            client.pubSubChannels() shouldBe emptyList()
            client.pubSubShardChannels() shouldBe emptyList()
        }
    }

    @Test
    suspend fun `test PUBLISH command`() {
        client.subscribe("publishChannel") { _, _: String -> }
        eventually(1.seconds) {
            client.publish("publishChannel", "testMessage") shouldBe 1L
        }
    }

    @Test
    suspend fun `test command issued inside pubsub handler decodes correctly`() {
        val pingResult = AtomicReference<Result<String>>()
        client.subscribe("cmdInHandlerChannel") { _, _: String ->
            pingResult.set(runCatching { client.ping() })
        }
        eventually(1.seconds) {
            client.publish("cmdInHandlerChannel", "trigger") shouldBe 1L
        }
        eventually(5.seconds) {
            pingResult.get().shouldNotBeNull().getOrThrow() shouldBe "PONG"
        }
    }

    @Test
    suspend fun `test PUBSUB CHANNELS command`() {
        client.subscribe("pubSubChannelsChannel") { _, _: String -> }
        eventually(1.seconds) {
            client.pubSubChannels().shouldNotBeEmpty()
        }
    }

    @Test
    suspend fun `test PUBSUB NUMPAT command`() {
        client.pSubscribe("numPatTest*") { _, _: String -> }
        eventually(1.seconds) {
            client.pubSubNumPat() shouldBeGreaterThan 0
        }
    }

    @Test
    suspend fun `test PUBSUB NUMSUB command`() {
        client.subscribe("numSubChannel") { _, _: String -> }
        eventually(1.seconds) {
            client.pubSubNumSub("numSubChannel") shouldBe listOf(PubSubNumEntry("numSubChannel", 1))
        }
    }

    @Test
    suspend fun `test PUBSUB SHARDCHANNELS command`() {
        eventually(1.seconds) {
            client.pubSubShardChannels() shouldBe emptyList()
        }
    }

    @Test
    suspend fun `test PUBSUB SHARDNUMSUB command`() {
        eventually(1.seconds) {
            client.pubSubShardNumSub("shardNumSubChannel") shouldBe listOf(PubSubNumEntry("shardNumSubChannel", 0))
        }
    }

    @Test
    suspend fun `test PUNSUBSCRIBE command`() = shouldNotThrowAny {
        client.pUnsubscribe("punsubscribePattern")
    }

    @Test
    suspend fun `test SPUBLISH command`() {
        eventually(1.seconds) {
            client.sPublish("sPublishShardChannel", "testMessage") shouldBe 0L
        }
    }

    @Test
    suspend fun `test SUNSUBSCRIBE command`() = shouldNotThrowAny {
        client.sUnsubscribe("sunsubscribeChannel")
    }

    @Test
    suspend fun `test UNSUBSCRIBE command`() = shouldNotThrowAny {
        client.unsubscribe("unsubscribeChannel")
    }

    @Test
    suspend fun `test PSUBSCRIBE command`() {
        client.pSubscribe("pSubscribePattern") { _, _: String -> }
        eventually(1.seconds) {
            client.subscriptions.isActiveHandlers(SubscribeTarget.Pattern("pSubscribePattern")).shouldBeTrue()
        }
    }

    @Test
    suspend fun `test SSUBSCRIBE command`() {
        client.sSubscribe("sSubscribeShardChannel") { _, _: String -> }
        eventually(1.seconds) {
            client.subscriptions.isActiveHandlers(SubscribeTarget.Shard("sSubscribeShardChannel")).shouldBeTrue()
        }
    }

    @Test
    suspend fun `test SUBSCRIBE command`() {
        client.subscribe("subscribeChannel") { _, _: String -> }
        eventually(1.seconds) {
            client.subscriptions.isActiveHandlers(SubscribeTarget.Channel("subscribeChannel")).shouldBeTrue()
        }
    }

    @Test
    suspend fun `test unsubscription command`() {
        client.subscribe("unsubscriptionChannel") { _, _: String -> }
        eventually(1.seconds) {
            client.subscriptions.isActiveHandlers(SubscribeTarget.Channel("unsubscriptionChannel")).shouldBeTrue()
        }

        client.subscriptions.unsubscribe(SubscribeTarget.Channel("unsubscriptionChannel"))
        eventually(1.seconds) {
            client.subscriptions.isActiveHandlers(SubscribeTarget.Channel("unsubscriptionChannel")).shouldBe(false)
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
