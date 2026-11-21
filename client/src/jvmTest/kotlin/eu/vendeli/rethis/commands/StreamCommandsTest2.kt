package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.stream.*
import eu.vendeli.rethis.shared.request.common.FieldValue
import eu.vendeli.rethis.shared.request.stream.XAddOption
import eu.vendeli.rethis.shared.request.stream.XId
import eu.vendeli.rethis.shared.request.stream.XReadGroupKeyIds
import eu.vendeli.rethis.shared.types.BulkString
import eu.vendeli.rethis.shared.types.Int64
import eu.vendeli.rethis.shared.types.RArray
import io.kotest.matchers.shouldBe

class StreamCommandsTest2 : ReThisTestCtx() {
    @Test
    suspend fun `test XAUTCLAIM command`() {
        val streamName = "mystream-1"
        val groupName = "mygroup-1"
        client.xGroupCreate(streamName, groupName, XId.Id("0"), mkstream = true)
        val id = client.xAdd(
            streamName,
            idSelector = XAddOption.Asterisk,
            data = arrayOf(
                FieldValue(
                    "field1",
                    "value1",
                ),
            ),
        )!!
        // deliver the entry to another consumer so it is pending and claimable
        client
            .xReadGroup(
                group = groupName,
                consumer = "owner-1",
                streams = XReadGroupKeyIds(key = listOf(streamName), id = listOf(">")),
            )!!
            .single()
            .messages
            .map { it.id } shouldBe listOf(id)

        val autoClaimed = client.xAutoClaim(
            key = streamName,
            group = groupName,
            consumer = "myconsumer-1",
            minIdleTime = "0",
            start = id,
            count = 1,
        )
        autoClaimed shouldBe listOf(
            BulkString("0-0"),
            RArray(listOf(streamEntry(id, "field1", "value1"))),
            RArray(emptyList()),
        )
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
        val id = client.xAdd(
            streamName,
            idSelector = XAddOption.Asterisk,
            data = arrayOf(FieldValue("field1", "value1")),
        )!!
        val streamInfo = client.xInfoStream(streamName)
        streamInfo["length"] shouldBe Int64(1)
        streamInfo["groups"] shouldBe Int64(0)
        streamInfo["last-generated-id"] shouldBe BulkString(id)
        streamInfo["first-entry"] shouldBe streamEntry(id, "field1", "value1")
        streamInfo["last-entry"] shouldBe streamEntry(id, "field1", "value1")
    }

    @Test
    suspend fun `test XRANGE command`() {
        val streamName = "mystream-4"
        val id1 = client.xAdd(
            streamName,
            idSelector = XAddOption.Asterisk,
            data = arrayOf(FieldValue("field1", "value1")),
        )!!
        val id2 = client.xAdd(
            streamName,
            idSelector = XAddOption.Asterisk,
            data = arrayOf(FieldValue("field2", "value2")),
        )!!
        val rangeResult = client.xRange(streamName, start = "-", end = "+")
        rangeResult shouldBe listOf(streamEntry(id1, "field1", "value1"), streamEntry(id2, "field2", "value2"))
    }

    @Test
    suspend fun `test XREVRANGE command`() {
        val streamName = "mystream-5"
        val id1 = client.xAdd(
            streamName,
            idSelector = XAddOption.Asterisk,
            data = arrayOf(FieldValue("field1", "value1")),
        )!!
        val id2 = client.xAdd(
            streamName,
            idSelector = XAddOption.Asterisk,
            data = arrayOf(FieldValue("field2", "value2")),
        )!!
        val revRangeResult = client.xRevRange(streamName, end = "+", start = "-")
        revRangeResult shouldBe listOf(streamEntry(id2, "field2", "value2"), streamEntry(id1, "field1", "value1"))
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
