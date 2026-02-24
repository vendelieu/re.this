package eu.vendeli.rethis.wrappers

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.stream.xAck
import eu.vendeli.rethis.command.stream.xAdd
import eu.vendeli.rethis.command.stream.xGroupCreate
import eu.vendeli.rethis.command.stream.xPending
import eu.vendeli.rethis.command.stream.xRead
import eu.vendeli.rethis.command.stream.xReadGroup
import eu.vendeli.rethis.shared.request.common.FieldValue
import eu.vendeli.rethis.shared.request.stream.XAddOption
import eu.vendeli.rethis.shared.request.stream.XId
import eu.vendeli.rethis.shared.request.stream.XReadGroupKeyIds
import eu.vendeli.rethis.shared.types.BulkString
import eu.vendeli.rethis.shared.types.PlainString
import eu.vendeli.rethis.shared.types.RType
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeout
import kotlinx.io.readString
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ReThisStreamFlowTest : ReThisTestCtx() {
    @Test
    suspend fun `test StreamFlow consumption`() = coroutineScope {
        val key = "stream1"
        val group = "group1"
        val consumer = "consumer1"

        client.xGroupCreate(key, group, XId.Id("0"), mkstream = true)
        client.xAdd(key, null, null, XAddOption.Asterisk, FieldValue("f1", "v1"))

        val flow = client.StreamFlow(
            key = key,
            group = group,
            consumer = consumer,
            block = 1.seconds,
        )

        val collected = withTimeout(5.seconds) {
            flow.take(1).toList()
        }

        collected shouldHaveSize 1
        collected.first().data["f1"].asString() shouldBe "v1"
    }

    @Test
    suspend fun `test StreamFlow auto-acknowledgment`() = coroutineScope {
        val key = "stream_ack"
        val group = "group_ack"
        val consumer = "consumer_ack"

        client.xGroupCreate(key, group, XId.Id("0"), mkstream = true)
        client.xAdd(key, null, null, XAddOption.Asterisk, FieldValue("k", "v"))

        // Consume with auto-ack (default)
        withTimeout(5.seconds) {
            client.StreamFlow(key, group, consumer, block = 100.milliseconds)
                .take(1)
                .collect {}
        }

        // Verify PEL is empty
        eventually(2.seconds) {
            val pending = client.xPending(key, group)
            pending.first().value.toString().toLong() shouldBe 0L
        }
    }

    @Test
    suspend fun `test StreamFlow without auto-acknowledgment`() = coroutineScope {
        val key = "stream_no_ack"
        val group = "group_no_ack"
        val consumer = "consumer_no_ack"

        client.xGroupCreate(key, group, XId.Id("0"), mkstream = true)
        client.xAdd(key, null, null, XAddOption.Asterisk, FieldValue("k", "v"))

        // Consume WITHOUT auto-ack
        withTimeout(5.seconds) {
            client.StreamFlow(key, group, consumer, acknowledge = false, block = 100.milliseconds)
                .take(1)
                .collect {}
        }

        // Verify PEL is NOT empty
        val pending = client.xPending(key, group)
        pending.first().value.toString().toLong() shouldBe 1L
    }

    @Test
    suspend fun `test StreamFlow batch consumption`() = coroutineScope {
        val key = "stream_batch"
        val group = "group_batch"
        val consumer = "consumer_batch"

        client.xGroupCreate(key, group, XId.Id("0"), mkstream = true)
        repeat(5) { i ->
            client.xAdd(key, null, null, XAddOption.Asterisk, FieldValue("i", i.toString()))
        }

        val collected = withTimeout(5.seconds) {
            client.StreamFlow(key, group, consumer, batchSize = 2, block = 100.milliseconds)
                .take(5)
                .toList()
        }

        collected shouldHaveSize 5
        collected.map { it.data["i"].asString() } shouldBe listOf("0", "1", "2", "3", "4")
    }

    private fun RType?.asString(): String = when (this) {
        is BulkString -> value.readString()
        is PlainString -> value
        else -> this?.value?.toString() ?: ""
    }
}
