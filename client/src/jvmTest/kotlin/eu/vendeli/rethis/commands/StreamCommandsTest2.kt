package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.stream.*
import eu.vendeli.rethis.shared.request.common.FieldValue
import eu.vendeli.rethis.shared.request.stream.XAddOption
import eu.vendeli.rethis.shared.request.stream.XId
import eu.vendeli.rethis.shared.types.RType
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class StreamCommandsTest2 : ReThisTestCtx() {
    @Test
    suspend fun `test XAUTCLAIM command`() {
        val streamName = "mystream-1"
        val groupName = "mygroup-1"
        client.xGroupCreate(streamName, groupName, XId.Id("0"), mkstream = true)
        val id = client.xAdd(
            streamName, idSelector = XAddOption.Asterisk,
            data = arrayOf(
                FieldValue(
                    "field1",
                    "value1",
                ),
            ),
        )!!
        val autoClaimed = client.xAutoClaim(
            key = streamName,
            group = groupName,
            consumer = "myconsumer-1",
            minIdleTime = "1000",
            start = id,
            count = 1,
        )
        autoClaimed shouldNotBe null
    }

    @Test
    suspend fun `test XGROUP SETID command`() {
        val streamName = "mystream-2"
        val groupName = "mygroup-2"
        client.xGroupCreate(streamName, groupName, XId.Id("0"), mkstream = true)
        val result = client.xGroupSetId(streamName, groupName, XId.Id("1"), entriesread = 0)
        result shouldBe true
    }

    @Test
    suspend fun `test XINFO STREAM command`() {
        val streamName = "mystream-3"
        client.xAdd(streamName, idSelector = XAddOption.Asterisk, data = arrayOf(FieldValue("field1", "value1")))
        val streamInfo = client.xInfoStream(streamName)
        streamInfo shouldNotBe emptyMap<String, RType>()
    }

    @Test
    suspend fun `test XRANGE command`() {
        val streamName = "mystream-4"
        client.xAdd(streamName, idSelector = XAddOption.Asterisk, data = arrayOf(FieldValue("field1", "value1")))
        client.xAdd(streamName, idSelector = XAddOption.Asterisk, data = arrayOf(FieldValue("field2", "value2")))
        val rangeResult = client.xRange(streamName, start = "-", end = "+")
        rangeResult shouldNotBe emptyList<RType>()
    }

    @Test
    suspend fun `test XREVRANGE command`() {
        val streamName = "mystream-5"
        client.xAdd(streamName, idSelector = XAddOption.Asterisk, data = arrayOf(FieldValue("field1", "value1")))
        client.xAdd(streamName, idSelector = XAddOption.Asterisk, data = arrayOf(FieldValue("field2", "value2")))
        val revRangeResult = client.xRevRange(streamName, end = "+", start = "-")
        revRangeResult shouldNotBe emptyList<RType>()
    }

    @Test
    suspend fun `test XSETID command`() {
        val streamName = "mystream-6"
        val groupName = "mygroup-6"
        client.xGroupCreate(streamName, groupName, XId.Id("0"), mkstream = true)
        val result = client.xSetId(streamName, lastId = "1", entriesAdded = 1)
        result shouldBe true
    }
}
