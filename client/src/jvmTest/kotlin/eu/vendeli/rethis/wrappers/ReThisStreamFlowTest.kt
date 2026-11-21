package eu.vendeli.rethis.wrappers

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.stream.xAdd
import eu.vendeli.rethis.command.stream.xGroupCreate
import eu.vendeli.rethis.command.stream.xDel
import eu.vendeli.rethis.command.stream.xPending
import eu.vendeli.rethis.command.stream.xReadGroup
import eu.vendeli.rethis.shared.request.stream.XReadGroupKeyIds
import eu.vendeli.rethis.shared.request.stream.XReadGroupOption
import eu.vendeli.rethis.shared.request.common.FieldValue
import eu.vendeli.rethis.shared.request.stream.XAddOption
import eu.vendeli.rethis.shared.request.stream.XId
import eu.vendeli.rethis.shared.types.BulkString
import eu.vendeli.rethis.shared.types.RArray
import eu.vendeli.rethis.shared.types.PlainString
import eu.vendeli.rethis.shared.types.RType
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.readString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class ReThisStreamFlowTest : ReThisTestCtx() {
    @Test
    suspend fun `test StreamFlow consumption`() = coroutineScope {
        val key = "stream1"
        val group = "group1"
        val consumer = "consumer1"

        client.xGroupCreate(key, group, XId.Id("0"), mkstream = true)

        val flow = client.StreamFlow(
            key = key,
            group = group,
            consumer = consumer,
            block = 1.seconds,
        )

        val deferred = async {
            flow.take(1).toList()
        }
        delay(300)
        client.xAdd(key, null, null, XAddOption.Asterisk, FieldValue("f1", "v1"))

        val collected = withTimeout(10.seconds) {
            deferred.await()
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
        repeat(3) { i ->
            client.xAdd(key, null, null, XAddOption.Asterisk, FieldValue("i", i.toString()))
        }

        val collected = withTimeout(10.seconds) {
            client
                .StreamFlow(key, group, consumer, block = 100.milliseconds)
                .take(3)
                .toList()
        }

        collected shouldHaveSize 3
        // the last element is aborted inside emit by take(), so it is not acknowledged
        client
            .xPending(key, group)
            .first()
            .value
            .toString()
            .toLong() shouldBe 1L
    }

    @Test
    suspend fun `test StreamFlow does not acknowledge the item rejected by takeWhile`() = coroutineScope {
        val key = "stream_take_while"
        val group = "group_take_while"

        client.xGroupCreate(key, group, XId.Id("0"), mkstream = true)
        repeat(3) { i ->
            client.xAdd(key, null, null, XAddOption.Asterisk, FieldValue("i", i.toString()))
        }

        val collected = withTimeout(10.seconds) {
            client
                .StreamFlow(key, group, "consumer_take_while", block = 100.milliseconds)
                .takeWhile { it.data["i"].asString() != "2" }
                .toList()
        }

        collected shouldHaveSize 2
        client
            .xPending(key, group)
            .first()
            .value
            .toString()
            .toLong() shouldBe 1L
    }

    @Test
    suspend fun `test StreamFlow without auto-acknowledgment`() = coroutineScope {
        val key = "stream_no_ack"
        val group = "group_no_ack"
        val consumer = "consumer_no_ack"

        client.xGroupCreate(key, group, XId.Id("0"), mkstream = true)

        val deferred = async {
            client
                .StreamFlow(key, group, consumer, acknowledge = false, block = 100.milliseconds)
                .take(1)
                .toList()
        }
        delay(300)
        client.xAdd(key, null, null, XAddOption.Asterisk, FieldValue("k", "v"))

        withTimeout(10.seconds) {
            deferred.await()
        }

        val pending = client.xPending(key, group)
        pending
            .first()
            .value
            .toString()
            .toLong() shouldBe 1L
    }

    @Test
    suspend fun `test StreamFlow batch consumption`() = coroutineScope {
        val key = "stream_batch"
        val group = "group_batch"
        val consumer = "consumer_batch"

        client.xGroupCreate(key, group, XId.Id("0"), mkstream = true)

        val deferred = async {
            client
                .StreamFlow(key, group, consumer, batchSize = 2, block = 100.milliseconds)
                .take(5)
                .toList()
        }
        delay(300)
        repeat(5) { i ->
            client.xAdd(key, null, null, XAddOption.Asterisk, FieldValue("i", i.toString()))
        }

        val collected = withTimeout(10.seconds) {
            deferred.await()
        }

        collected shouldHaveSize 5
        collected.map { it.data["i"].asString() } shouldBe listOf("0", "1", "2", "3", "4")
    }

    @Test
    suspend fun `test StreamFlow keeps message pending when collector throws`() = coroutineScope {
        val key = "stream_throw"
        val group = "group_throw"

        client.xGroupCreate(key, group, XId.Id("0"), mkstream = true)
        client.xAdd(key, null, null, XAddOption.Asterisk, FieldValue("k", "v"))

        shouldThrow<IllegalStateException> {
            withTimeout(10.seconds) {
                client
                    .StreamFlow(key, group, "consumer_throw", block = 100.milliseconds)
                    .collect { error("collector failure") }
            }
        }

        client
            .xPending(key, group)
            .first()
            .value
            .toString()
            .toLong() shouldBe 1L
    }

    @Test
    suspend fun `test StreamFlow keeps message pending when collector is cancelled`() = coroutineScope {
        val key = "stream_cancel"
        val group = "group_cancel"

        client.xGroupCreate(key, group, XId.Id("0"), mkstream = true)
        client.xAdd(key, null, null, XAddOption.Asterisk, FieldValue("k", "v"))

        val received = CompletableDeferred<Unit>()
        val job = launch {
            client
                .StreamFlow(key, group, "consumer_cancel", block = 100.milliseconds)
                .collect {
                    received.complete(Unit)
                    awaitCancellation()
                }
        }
        withTimeout(10.seconds) { received.await() }
        job.cancelAndJoin()

        client
            .xPending(key, group)
            .first()
            .value
            .toString()
            .toLong() shouldBe 1L
    }

    @Test
    suspend fun `test StreamFlow drains a backlog without sleeping between batches`() = coroutineScope {
        val key = "stream_backlog"
        val group = "group_backlog"
        val total = 30

        client.xGroupCreate(key, group, XId.Id("0"), mkstream = true)
        repeat(total) { i ->
            client.xAdd(key, null, null, XAddOption.Asterisk, FieldValue("i", i.toString()))
        }

        val elapsed = measureTime {
            val collected = withTimeout(20.seconds) {
                client
                    .StreamFlow(key, group, "consumer_backlog", batchSize = 1, block = Duration.ZERO)
                    .take(total)
                    .toList()
            }
            collected shouldHaveSize total
        }

        // one message per batch: the former per-batch sleep alone cost 30 * 100 ms = 3 s
        (elapsed < 2.seconds) shouldBe true
    }

    @Test
    suspend fun `test StreamFlow redelivers and acknowledges the consumer's pending entries first`() = coroutineScope {
        val key = "stream_redeliver"
        val group = "group_redeliver"
        val consumer = "consumer_redeliver"

        client.xGroupCreate(key, group, XId.Id("0"), mkstream = true)
        repeat(3) { i ->
            client.xAdd(key, null, null, XAddOption.Asterisk, FieldValue("i", i.toString()))
        }
        // delivered to the consumer but never acknowledged
        client.xReadGroup(
            group,
            consumer,
            XReadGroupKeyIds(listOf(key), listOf(">")),
            XReadGroupOption.Count(2),
        )
        client.xAdd(key, null, null, XAddOption.Asterisk, FieldValue("i", "3"))

        val collected = withTimeout(10.seconds) {
            client
                .StreamFlow(key, group, consumer, block = 100.milliseconds)
                .take(4)
                .toList()
        }

        collected.map { it.data["i"].asString() } shouldBe listOf("0", "1", "2", "3")
        // only the last element, aborted inside emit by take(), is still pending
        client
            .xPending(key, group)
            .first()
            .value
            .toString()
            .toLong() shouldBe 1L
    }

    @Test
    suspend fun `test StreamFlow redelivers the entry left pending by a cancelled emit`() = coroutineScope {
        val key = "stream_redeliver_cancel"
        val group = "group_redeliver_cancel"
        val consumer = "consumer_redeliver_cancel"

        client.xGroupCreate(key, group, XId.Id("0"), mkstream = true)
        client.xAdd(key, null, null, XAddOption.Asterisk, FieldValue("k", "v"))

        val received = CompletableDeferred<Unit>()
        val job = launch {
            client.StreamFlow(key, group, consumer, block = 100.milliseconds).collect {
                received.complete(Unit)
                awaitCancellation()
            }
        }
        withTimeout(10.seconds) { received.await() }
        job.cancelAndJoin()
        client
            .xPending(key, group)
            .first()
            .value
            .toString()
            .toLong() shouldBe 1L

        val ids = withTimeout(10.seconds) {
            client.StreamFlow(key, group, consumer, block = 100.milliseconds).take(1).toList()
        }
        ids.single().data["k"].asString() shouldBe "v"
    }

    @Test
    suspend fun `test StreamFlow without acknowledgment delivers each pending entry once per start`() = coroutineScope {
        val key = "stream_redeliver_noack"
        val group = "group_redeliver_noack"
        val consumer = "consumer_redeliver_noack"

        client.xGroupCreate(key, group, XId.Id("0"), mkstream = true)
        repeat(3) { i ->
            client.xAdd(key, null, null, XAddOption.Asterisk, FieldValue("i", i.toString()))
        }
        client.xReadGroup(group, consumer, XReadGroupKeyIds(listOf(key), listOf(">")))

        val collected = withTimeout(10.seconds) {
            client
                .StreamFlow(key, group, consumer, batchSize = 2, block = 100.milliseconds, acknowledge = false)
                .take(3)
                .toList()
        }

        collected.map { it.data["i"].asString() } shouldBe listOf("0", "1", "2")
    }

    @Test
    suspend fun `test StreamFlow takes over entries idle for another consumer when claimIdleAfter is set`() =
        coroutineScope {
            val key = "stream_claim"
            val group = "group_claim"

            client.xGroupCreate(key, group, XId.Id("0"), mkstream = true)
            repeat(3) { i ->
                client.xAdd(key, null, null, XAddOption.Asterisk, FieldValue("i", i.toString()))
            }
            client.xReadGroup(group, "consumer_a", XReadGroupKeyIds(listOf(key), listOf(">")))
            delay(200)

            val received = mutableListOf<String>()
            val job = launch {
                client
                    .StreamFlow(key, group, "consumer_b", block = 100.milliseconds, claimIdleAfter = 100.milliseconds)
                    .collect { received += it.data["i"].asString() }
            }
            eventually(10.seconds) { pendingCount(key, group) shouldBe 0L }
            job.cancelAndJoin()

            received shouldBe listOf("0", "1", "2")
        }

    @Test
    suspend fun `test StreamFlow does not take over another consumer's entries without claimIdleAfter`() =
        coroutineScope {
            val key = "stream_no_claim"
            val group = "group_no_claim"

            client.xGroupCreate(key, group, XId.Id("0"), mkstream = true)
            client.xAdd(key, null, null, XAddOption.Asterisk, FieldValue("i", "0"))
            client.xReadGroup(group, "consumer_a", XReadGroupKeyIds(listOf(key), listOf(">")))
            delay(200)

            val received = mutableListOf<String>()
            withTimeoutOrNull(1.seconds) {
                client
                    .StreamFlow(key, group, "consumer_b", block = 100.milliseconds)
                    .collect { received += it.data["i"].asString() }
            }

            received shouldBe emptyList()
            pendingCount(key, group) shouldBe 1L
            val owners = (client.xPending(key, group)[3] as RArray)
                .value
                .map { ((it as RArray).value.first()).asString() }
            owners shouldBe listOf("consumer_a")
        }

    @Test
    suspend fun `test StreamFlow rejects a negative claimIdleAfter`() = coroutineScope {
        shouldThrow<IllegalArgumentException> {
            client.StreamFlow("stream_negative", "g", "c", claimIdleAfter = (-1).seconds).collect { }
        }
        Unit
    }

    @Test
    fun `test parseAutoClaim handles the 2-element reply and a nil entry`() {
        val reply = listOf<RType>(
            BulkString("0-0"),
            RArray(
                listOf(
                    RArray(listOf(BulkString("1-0"), RArray(listOf(BulkString("f"), BulkString("v"))))),
                    RArray(listOf(BulkString("2-0"), RType.Null)),
                ),
            ),
        )

        val page = parseAutoClaim(reply)

        page.nextCursor shouldBe "0-0"
        page.messages.map { it.id } shouldBe listOf("1-0", "2-0")
        page.messages[0].data["f"].asString() shouldBe "v"
        page.messages[1].data shouldBe emptyMap()
    }

    @Test
    fun `test parseAutoClaim ignores the deleted ids element of the 3-element reply`() {
        val reply = listOf<RType>(BulkString("5-0"), RArray(emptyList()), RArray(listOf(BulkString("3-0"))))

        val page = parseAutoClaim(reply)

        page.nextCursor shouldBe "5-0"
        page.messages shouldBe emptyList()
    }

    @Test
    suspend fun `test StreamFlow skips and acknowledges a pending entry deleted from the stream`() = coroutineScope {
        val key = "stream_deleted"
        val group = "group_deleted"
        val consumer = "consumer_deleted"

        client.xGroupCreate(key, group, XId.Id("0"), mkstream = true)
        val id = client.xAdd(key, null, null, XAddOption.Asterisk, FieldValue("i", "gone"))!!
        client.xReadGroup(group, consumer, XReadGroupKeyIds(listOf(key), listOf(">")))
        pendingCount(key, group) shouldBe 1L
        client.xDel(key, id)

        val received = mutableListOf<String>()
        withTimeoutOrNull(1.seconds) {
            client
                .StreamFlow(key, group, consumer, block = 100.milliseconds)
                .collect { received += it.id }
        }

        received shouldBe emptyList()
        pendingCount(key, group) shouldBe 0L
    }

    private suspend fun pendingCount(key: String, group: String): Long = client
        .xPending(key, group)
        .first()
        .value
        .toString()
        .toLong()

    private fun RType?.asString(): String = when (this) {
        is BulkString -> value.readString()
        is PlainString -> value
        else -> this?.value?.toString().orEmpty()
    }
}
