package eu.vendeli.rethis.tests.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.*
import eu.vendeli.rethis.types.core.RType
import eu.vendeli.rethis.types.options.MAXLEN
import eu.vendeli.rethis.types.options.XAddOption
import eu.vendeli.rethis.types.options.XId
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class StreamCommandsTest : ReThisTestCtx() {
    @Test
    suspend fun `test XADD command`() {
        val result = client.xAdd(
            key = "mystream1",
            id = XAddOption.Asterisk,
            entry = arrayOf("field1" to "value1", "field2" to "value2"),
        )
        result shouldNotBe null
    }

    @Test
    suspend fun `test XACK command`() {
        client.xGroupCreate("mystream2", "mygroup2", XId.Id("0"), mkstream = true)
        client.xReadGroup(
            group = "mygroup2",
            consumer = "myconsumer2",
            keys = listOf("mystream2"),
            ids = listOf("0"),
        )

        val id = client.xAdd(
            key = "mystream2",
            id = XAddOption.Asterisk,
            entry = arrayOf("field1" to "value1"),
        )!!
        val ackCount = client.xAck("mystream2", "mygroup2", id)
        ackCount shouldNotBe null
    }

    @Test
    suspend fun `test XDEL command`() {
        val id = client.xAdd("mystream3", id = XAddOption.Asterisk, entry = arrayOf("field1" to "value1"))!!
        val delCount = client.xDel("mystream3", id)
        delCount shouldBe 1L
    }

    @Test
    suspend fun `test XGROUP CREATE command`() {
        val result = client.xGroupCreate("mystream4", "mygroup4", XId.Id("0"), mkstream = true)
        result shouldBe true
    }

    @Test
    suspend fun `test XGROUP CREATECONSUMER command`() {
        client.xGroupCreate("mystream5", "mygroup5", XId.Id("0"), mkstream = true)
        val consumerCount = client.xGroupCreateConsumer("mystream5", "mygroup5", "myconsumer5")
        consumerCount shouldBe 1L
    }

    @Test
    suspend fun `test XGROUP DELCONSUMER command`() {
        client.xGroupCreate("mystream6", "mygroup6", XId.Id("0"), mkstream = true)
        client.xGroupCreateConsumer("mystream6", "mygroup6", "myconsumer6")
        val delCount = client.xGroupDelConsumer("mystream6", "mygroup6", "myconsumer6")
        delCount shouldNotBe null
    }

    @Test
    suspend fun `test XGROUP DESTROY command`() {
        client.xGroupCreate("mystream7", "mygroup7", XId.Id("0"), mkstream = true)
        val destroyCount = client.xGroupDestroy("mystream7", "mygroup7")
        destroyCount shouldBe 1L
    }

    @Test
    suspend fun `test XINFO CONSUMERS command`() {
        client.xGroupCreate("mystream8", "mygroup8", XId.Id("0"), mkstream = true)
        client.xGroupCreateConsumer("mystream8", "mygroup8", "myconsumer8")
        val consumers = client.xInfoConsumers("mystream8", "mygroup8")
        consumers shouldNotBe emptyList<RType>()
    }

    @Test
    suspend fun `test XINFO GROUPS command`() {
        client.xGroupCreate("mystream9", "mygroup9", XId.Id("0"), mkstream = true)
        val groups = client.xInfoGroups("mystream9")
        groups shouldNotBe emptyList<RType>()
    }

    @Test
    suspend fun `test XLEN command`() {
        client.xAdd("mystream10", id = XAddOption.Asterisk, entry = arrayOf("field1" to "value1"))
        client.xAdd("mystream11", id = XAddOption.Asterisk, entry = arrayOf("field2" to "value2"))
        val length = client.xLen("mystream10")
        length shouldBe 1L
        client.xLen("mystream10") shouldBe 1L
    }

    @Test
    suspend fun `test XTRIM command`() {
        client.xAdd("mystream12", id = XAddOption.Asterisk, entry = arrayOf("field1" to "value1"))
        val trimmedCount = client.xTrim("mystream12", threshold = 1, trimmingStrategy = MAXLEN)
        trimmedCount shouldNotBe null
    }

    @Test
    suspend fun `test XREAD command`() {
        client.xAdd("mystream14", id = XAddOption.Asterisk, entry = arrayOf("field1" to "value1"))
        client.xAdd("mystream15", id = XAddOption.Asterisk, entry = arrayOf("field2" to "value2"))
        val result = client.xRead(keys = listOf("mystream15"), ids = listOf("0"))
        result shouldNotBe null
    }

    @Test
    suspend fun `test XREADGROUP command`() {
        client.xGroupCreate("mystream16", "mygroup16", XId.Id("0"), mkstream = true)
        client.xAdd("mystream16", id = XAddOption.Asterisk, entry = arrayOf("field1" to "value1"))
        val result = client.xReadGroup(
            group = "mygroup16",
            consumer = "myconsumer16",
            keys = listOf("mystream16"),
            ids = listOf("0"),
        )
        result shouldNotBe null
    }

    @Test
    suspend fun `test XCLAIM command`() {
        client.xGroupCreate("mystream17", "mygroup17", XId.Id("0"), mkstream = true)
        val id = client.xAdd("mystream17", id = XAddOption.Asterisk, entry = arrayOf("field1" to "value1"))!!
        val claimed = client.xClaim("mystream17", "mygroup17", "myconsumer17", minIdleTime = 1000, id = arrayOf(id))
        claimed shouldNotBe null
    }

    @Test
    suspend fun `test XPENDING command`() {
        client.xGroupCreate("mystream18", "mygroup18", XId.Id("0"), mkstream = true)
        client.xAdd("mystream18", id = XAddOption.Asterisk, entry = arrayOf("field1" to "value1"))
        val pending = client.xPending("mystream18", "mygroup18")
        pending shouldNotBe emptyList<RType>()
    }
}
