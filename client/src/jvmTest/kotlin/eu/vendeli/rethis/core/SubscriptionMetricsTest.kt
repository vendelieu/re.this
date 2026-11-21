package eu.vendeli.rethis.core

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.metrics.RecordingMetricsRecorder
import eu.vendeli.rethis.metrics.RecordingMetricsRecorder.MetricEvent.SubscriptionChange
import eu.vendeli.rethis.providers.ConnectionProvider
import eu.vendeli.rethis.shared.types.CommandRequest
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.topology.FakeRespServer
import eu.vendeli.rethis.types.common.Address
import eu.vendeli.rethis.types.common.PubSubKind
import eu.vendeli.rethis.types.common.RConnection
import eu.vendeli.rethis.types.common.SubscribeTarget
import eu.vendeli.rethis.types.interfaces.ExperimentalReThisMetricsApi
import eu.vendeli.rethis.types.interfaces.PubSubHandler
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Job
import kotlinx.io.Buffer

@OptIn(ExperimentalReThisMetricsApi::class)
class SubscriptionMetricsTest : TestCtx() {
    private val handler = object : PubSubHandler {
        override suspend fun onSubscribe(kind: PubSubKind, target: SubscribeTarget, subscribedChannels: Long) {}
        override suspend fun onUnsubscribe(kind: PubSubKind, target: SubscribeTarget, subscribedChannels: Long) {}
        override suspend fun onMessage(kind: PubSubKind, channel: String, message: RType, pattern: String?) {}
        override suspend fun onException(target: SubscribeTarget, ex: Exception) {}
    }

    private val otherHandler = object : PubSubHandler by handler {}

    private val provider = object : ConnectionProvider() {
        override val node: Address = Address("localhost", 6379)
        override suspend fun execute(request: CommandRequest): Buffer = Buffer()
        override fun close() {}
        override suspend fun borrowConnection(): RConnection = throw NotImplementedError()
        override suspend fun releaseConnection(conn: RConnection) {}
        override fun disposeConnection(conn: RConnection) {}
        override fun hasSpareConnection(): Boolean = false
    }

    private fun withManager(block: (SubscriptionManager, RecordingMetricsRecorder) -> Unit) {
        val recorder = RecordingMetricsRecorder()
        FakeRespServer().use { server ->
            val client = ReThis.standalone(server.address) {
                metricsRecorder = recorder
            }
            try {
                block(SubscriptionManager(client), recorder)
            } finally {
                client.close()
            }
        }
    }

    private fun RecordingMetricsRecorder.gauge() = events.filterIsInstance<SubscriptionChange>().sumOf { it.delta }

    @Test
    fun `several workers on one target count as a single active subscription`() = withManager { manager, recorder ->
        val target = SubscribeTarget.Channel("a")
        val jobs = List(3) { Job() }
        jobs.forEach { manager.registerSubscription(target, provider, handler, it) }
        manager.registerSubscription(target, provider, otherHandler, Job())

        recorder.gauge() shouldBe 1
    }

    @Test
    fun `the gauge returns to zero once every worker of a target is gone`() = withManager { manager, recorder ->
        val target = SubscribeTarget.Channel("a")
        val jobs = List(3) { Job() }
        jobs.forEach { manager.registerSubscription(target, provider, handler, it) }

        jobs.forEach { manager.unregisterHandler(target, handler, it) }

        recorder.gauge() shouldBe 0
    }

    @Test
    fun `unsubscribing a target decrements once and late worker cleanup does not decrement again`() =
        withManager { manager, recorder ->
            val target = SubscribeTarget.Channel("a")
            val other = SubscribeTarget.Channel("b")
            val jobs = List(2) { Job() }
            jobs.forEach { manager.registerSubscription(target, provider, handler, it) }
            manager.registerSubscription(other, provider, handler, Job())

            manager.unsubscribe(target)
            jobs.forEach { manager.unregisterHandler(target, handler, it) }

            recorder.gauge() shouldBe 1
        }
}
