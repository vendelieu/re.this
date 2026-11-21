package eu.vendeli.rethis.wrappers

import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.assertions.throwables.shouldThrow
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
    fun `clear on a missing key does not throw`() {
        val set = client.Set("clearMissingSet")
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

    @Test
    fun `addAll with an empty collection returns false`() {
        val set = client.Set("emptySet")
        set.addAll(emptyList()) shouldBe false
        set.isEmpty() shouldBe true
    }

    @Test
    fun `iterator remove writes through to redis`() {
        val set = client.Set("iterSet1")
        set.addAll(listOf("a", "b", "c"))
        val iterator = set.iterator()
        val removed = iterator.next()
        iterator.remove()
        client.Set("iterSet1").toSet() shouldBe setOf("a", "b", "c") - removed
    }

    @Test
    fun `removeAll writes through to redis`() {
        val set = client.Set("iterSet2")
        set.addAll(listOf("a", "b", "c"))
        set.removeAll(listOf("a")) shouldBe true
        client.Set("iterSet2").toSet() shouldBe setOf("b", "c")
    }

    @Test
    fun `retainAll writes through to redis`() {
        val set = client.Set("iterSet3")
        set.addAll(listOf("a", "b", "c"))
        set.retainAll(listOf("a")) shouldBe true
        client.Set("iterSet3").toSet() shouldBe setOf("a")
    }

    @Test
    fun `iterator remove before next throws`() {
        val set = client.Set("iterSet4")
        set.add("a")
        shouldThrow<IllegalStateException> { set.iterator().remove() }
    }

    @Test
    fun `iterator remove twice throws`() {
        val set = client.Set("iterSet5")
        set.add("a")
        val iterator = set.iterator()
        iterator.next()
        iterator.remove()
        shouldThrow<IllegalStateException> { iterator.remove() }
    }
}
