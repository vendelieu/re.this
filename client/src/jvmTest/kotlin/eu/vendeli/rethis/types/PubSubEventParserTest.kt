package eu.vendeli.rethis.types

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.shared.types.BulkString
import eu.vendeli.rethis.shared.types.Int64
import eu.vendeli.rethis.shared.types.PlainString
import eu.vendeli.rethis.shared.types.Push
import eu.vendeli.rethis.types.common.PubSubEvent
import eu.vendeli.rethis.types.common.PubSubEventParser
import eu.vendeli.rethis.types.common.PubSubKind
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class PubSubEventParserTest : TestCtx() {
    private fun push(vararg items: String) = Push(items.map { BulkString(it) })

    @Test
    fun `returns null for a message push without payload`() {
        PubSubEventParser.parse(push("message", "ch")).shouldBeNull()
    }

    @Test
    fun `returns null for a pmessage push without payload`() {
        PubSubEventParser.parse(push("pmessage", "p", "ch")).shouldBeNull()
    }

    @Test
    fun `returns null for a smessage push without payload`() {
        PubSubEventParser.parse(push("smessage", "ch")).shouldBeNull()
    }

    @Test
    fun `returns null for subscribe family pushes without a channel`() {
        listOf("subscribe", "unsubscribe", "psubscribe", "punsubscribe", "ssubscribe", "sunsubscribe").forEach {
            PubSubEventParser.parse(push(it)).shouldBeNull()
        }
    }

    @Test
    fun `returns null for a subscribe push without active count`() {
        PubSubEventParser.parse(push("subscribe", "ch")).shouldBeNull()
    }

    @Test
    fun `returns null when the channel or the count is not of the expected type`() {
        PubSubEventParser.parse(Push(listOf(BulkString("subscribe"), Int64(1), Int64(1)))).shouldBeNull()
        PubSubEventParser
            .parse(Push(listOf(BulkString("subscribe"), BulkString("ch"), BulkString("x"))))
            .shouldBeNull()
    }

    @Test
    fun `parses a well-formed message push`() {
        val payload = BulkString("data")

        val event = PubSubEventParser.parse(Push(listOf(BulkString("message"), BulkString("ch"), payload)))

        event.shouldBeInstanceOf<PubSubEvent.Message>()
        event.channel shouldBe "ch"
        event.payload shouldBe payload
        event.pattern.shouldBeNull()
        event.kind shouldBe PubSubKind.PLAIN
    }

    @Test
    fun `parses a well-formed pmessage push`() {
        val event = PubSubEventParser.parse(push("pmessage", "p*", "ch", "data"))

        event.shouldBeInstanceOf<PubSubEvent.Message>()
        event.channel shouldBe "ch"
        event.pattern shouldBe "p*"
        event.kind shouldBe PubSubKind.PATTERN
    }

    @Test
    fun `parses a well-formed subscribe push`() {
        val event = PubSubEventParser.parse(Push(listOf(PlainString("subscribe"), BulkString("ch"), Int64(1))))

        event.shouldBeInstanceOf<PubSubEvent.Subscribed>()
        event.activeCount shouldBe 1L
    }

    @Test
    fun `returns null for an empty or unknown push`() {
        PubSubEventParser.parse(Push(emptyList())).shouldBeNull()
        PubSubEventParser.parse(push("unknown", "x")).shouldBeNull()
    }
}
