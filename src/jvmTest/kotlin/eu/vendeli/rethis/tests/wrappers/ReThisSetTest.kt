package eu.vendeli.rethis.tests.wrappers

import eu.vendeli.rethis.wrappers.Set
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.shouldBe

class ReThisSetTest : ReThisTestCtx() {
    @Test
    fun `test add`() {
        val set = client.Set("testSet1")
        set.add("testValue")
        set.contains("testValue") shouldBe true
    }

    @Test
    fun `test get`() {
        val set = client.Set("testSet2")
        set.add("testValue")
        set.contains("testValue") shouldBe true
    }

    @Test
    fun `test remove`() {
        val set = client.Set("testSet3")
        set.add("testValue")
        set.remove("testValue")
        set.isEmpty() shouldBe true
    }

    @Test
    fun `test clear`() {
        val set = client.Set("testSet4")
        set.add("testValue")
        set.clear()
        set.isEmpty() shouldBe true
    }

    @Test
    fun `test size`() {
        val set = client.Set("testSet5")
        set.add("testValue")
        set.size shouldBe 1
    }

    @Test
    fun `test contains`() {
        val set = client.Set("testSet6")
        set.add("testValue")
        set.contains("testValue") shouldBe true
    }
}
