package eu.vendeli.rethis.tests.wrappers

import eu.vendeli.rethis.wrappers.Hash
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

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
        map.remove("testKey") shouldBe null
        map.containsKey("testKey") shouldBe false
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
}
