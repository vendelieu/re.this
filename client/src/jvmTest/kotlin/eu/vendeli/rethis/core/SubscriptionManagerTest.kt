 package eu.vendeli.rethis.core

 import eu.vendeli.rethis.ReThisTestCtx
 import eu.vendeli.rethis.providers.ConnectionProvider
 import eu.vendeli.rethis.shared.types.CommandRequest
 import eu.vendeli.rethis.shared.types.RType
 import eu.vendeli.rethis.types.common.Address
 import eu.vendeli.rethis.types.common.PubSubKind
 import eu.vendeli.rethis.types.common.RConnection
 import eu.vendeli.rethis.types.common.SubscribeTarget
 import eu.vendeli.rethis.types.interfaces.PubSubHandler
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Job
import kotlinx.io.Buffer
import org.junit.jupiter.api.BeforeEach

class SubscriptionManagerTest : ReThisTestCtx() {
    private var manager = SubscriptionManager()

    @BeforeEach
    fun setUp() {
        manager = SubscriptionManager()
    }

     private val testHandler = object : PubSubHandler {
         override suspend fun onSubscribe(kind: PubSubKind, target: SubscribeTarget, subscribedChannels: Long) {}
         override suspend fun onUnsubscribe(kind: PubSubKind, target: SubscribeTarget, subscribedChannels: Long) {}
         override suspend fun onMessage(kind: PubSubKind, channel: String, message: RType, pattern: String?) {}
         override suspend fun onException(target: SubscribeTarget, ex: Exception) {}
     }

     private val testProvider = object : ConnectionProvider() {
         override val node: Address = Address("localhost", 6379)
         override suspend fun execute(request: CommandRequest): Buffer = Buffer()
         override fun close() {}
         override suspend fun borrowConnection(): RConnection = throw NotImplementedError()
         override suspend fun releaseConnection(conn: RConnection) {}
         override fun disposeConnection(conn: RConnection) {}
         override fun hasSpareConnection(): Boolean = false
     }

     @Test
     fun `registerSubscription should add subscription and handler`() {
         val target = SubscribeTarget.Channel("test")
         val job = Job()
         manager.registerSubscription(target, testProvider, testHandler, job)

         manager.size shouldBe 1
         manager.activeSubscriptions[target].shouldNotBeNull()
         manager.activeSubscriptions[target]?.handlers?.get(testHandler)?.contains(job) shouldBe true
     }

    @Test
    fun `unregisterHandler should remove job from handler`() {
        val target = SubscribeTarget.Channel("test")
        val job1 = Job()
        val job2 = Job()
        manager.registerSubscription(target, testProvider, testHandler, job1)
        manager.registerSubscription(target, testProvider, testHandler, job2)
        manager.unregisterHandler(target, testHandler, job1)

        manager.activeSubscriptions[target]?.handlers?.get(testHandler)?.contains(job1) shouldBe false
        manager.activeSubscriptions[target]?.handlers?.get(testHandler)?.contains(job2) shouldBe true
    }

    @Test
    fun `unregisterHandler should prune empty entries`() {
        val target = SubscribeTarget.Channel("test")
        val job = Job()
        manager.registerSubscription(target, testProvider, testHandler, job)
        manager.unregisterHandler(target, testHandler, job)

        manager.activeSubscriptions[target] shouldBe null
        manager.size shouldBe 0
    }

     @Test
     fun `unsubscribe should cancel all jobs and remove target`() {
         val target = SubscribeTarget.Channel("test")
         val job1 = Job()
         val job2 = Job()
         manager.registerSubscription(target, testProvider, testHandler, job1)
         manager.registerSubscription(target, testProvider, testHandler, job2)

         manager.unsubscribe(target)

         job1.isCancelled shouldBe true
         job2.isCancelled shouldBe true
         manager.size shouldBe 0
         manager.activeSubscriptions[target] shouldBe null
     }

     @Test
     fun `unsubscribeAll should clear all subscriptions`() {
         val target1 = SubscribeTarget.Channel("test1")
         val target2 = SubscribeTarget.Channel("test2")
         manager.registerSubscription(target1, testProvider, testHandler, Job())
         manager.registerSubscription(target2, testProvider, testHandler, Job())

         manager.unsubscribeAll()

         manager.size shouldBe 0
     }

     @Test
     fun `global handlers registration`() {
         manager.registerGlobalHandler(testHandler)
         manager.globalHandlers.contains(testHandler) shouldBe true

         manager.unregisterGlobalHandler(testHandler)
         manager.globalHandlers.contains(testHandler) shouldBe false
     }

     @Test
     fun `isActiveHandlers should return correct status`() {
         val target = SubscribeTarget.Channel("test")
         manager.isActiveHandlers(target) shouldBe false

         manager.registerSubscription(target, testProvider, testHandler, Job())
         manager.isActiveHandlers(target) shouldBe true

         manager.unsubscribe(target)
         manager.isActiveHandlers(target) shouldBe false
     }
 }
