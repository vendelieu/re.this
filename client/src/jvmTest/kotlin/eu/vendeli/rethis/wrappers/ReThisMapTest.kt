package eu.vendeli.rethis.wrappers

import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class ReThisMapTest : ReThisTestCtx() {
    @Test
    fun `test put and get`() {
        val map = client.Hash("testMap1")
        map["testKey"] = "testValue"
        map["testKey"] shouldBe "testValue"
    }

    @Test
    fun `test containsKey`() {
        val map = client.Hash("testMap2")
        map["testKey"] = "testValue"
        map.containsKey("testKey") shouldBe true
    }

    @Test
    fun `test containsValue`() {
        val map = client.Hash("testMap3")
        map["testKey"] = "testValue"
        map.containsValue("testValue") shouldBe true
    }

    @Test
    fun `test clear`() {
        val map = client.Hash("testMap4")
        map["testKey"] = "testValue"
        map.clear()
        map.isEmpty() shouldBe true
    }

    @Test
    fun `test putAll`() {
        val map = client.Hash("testMap5")
        val otherMap = mapOf("testKey1" to "testValue1", "testKey2" to "testValue2")
        map.putAll(otherMap)
        map["testKey1"] shouldBe "testValue1"
        map["testKey2"] shouldBe "testValue2"
    }

    @Test
    fun `test remove`() {
        val map = client.Hash("testMap6")
        map["testKey"] = "testValue"
        map.remove("testKey") shouldBe "testValue"
        map.containsKey("testKey") shouldBe false
        map.remove("testKey") shouldBe null
    }

    @Test
    fun `put returns the previous value`() {
        val map = client.Hash("testMapPut")
        map.put("k", "v1") shouldBe null
        map.put("k", "v2") shouldBe "v1"
        map["k"] shouldBe "v2"
    }

    @Test
    fun `remove returns the previous value once`() {
        val map = client.Hash("testMapRemove")
        map["k"] = "v2"
        map.remove("k") shouldBe "v2"
        map.remove("k") shouldBe null
    }

    @Test
    fun `entry setValue returns the previous value`() {
        val map = client.Hash("testMapSetValue")
        map["k"] = "v2"
        val entry = map.entries.single()
        entry.setValue("v3") shouldBe "v2"
        map["k"] shouldBe "v3"
    }

    @Test
    fun `test size`() {
        val map = client.Hash("testMap7")
        map["testKey"] = "testValue"
        map.size shouldBe 1
    }

    @Test
    fun `test keys`() {
        val map = client.Hash("testMap8")
        map["testKey"] = "testValue"
        map.keys shouldBe setOf("testKey")
    }

    @Test
    fun `test values`() {
        val map = client.Hash("testMap9")
        map["testKey"] = "testValue"
        map.values shouldBe listOf("testValue")
    }

    @Test
    fun `test entries`() {
        val map = client.Hash("testMap10")
        map["testKey"] = "testValue"
        map["testKey"] shouldBe "testValue"
        map.entries
            .shouldHaveSize(1)
            .first()
            .toPair() shouldBe ("testKey" to "testValue")
    }

    @Test
    fun `clear on a missing key does not throw`() {
        val map = client.Hash("emptyMap")
        map.clear()
        map.isEmpty() shouldBe true
    }

    @Test
    fun `putAll with an empty map does not throw`() {
        val map = client.Hash("emptyMapPutAll")
        map.putAll(emptyMap())
        map.isEmpty() shouldBe true
    }
}
