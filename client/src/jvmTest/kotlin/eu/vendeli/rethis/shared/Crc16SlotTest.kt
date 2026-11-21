package eu.vendeli.rethis.shared

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.shared.utils.CRC16
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class Crc16SlotTest : TestCtx() {
    private fun slot(key: String) = CRC16.slot(key.toByteArray())

    @Test
    fun `crc matches the reference check value`() {
        CRC16.lookup("123456789".toByteArray()) shouldBe 0x31C3
    }

    @Test
    fun `slot matches the reference values`() {
        slot("foo") shouldBe 12182
        slot("bar") shouldBe 5061
        slot("") shouldBe 0
    }

    @Test
    fun `keys sharing a hash tag share a slot`() {
        slot("{user1000}.following") shouldBe slot("{user1000}.followers")
        slot("{user1000}.following") shouldBe slot("user1000")
        slot("{a}1") shouldBe slot("{a}2")
    }

    @Test
    fun `empty tag hashes the whole key`() {
        slot("foo{}bar") shouldBe CRC16.lookup("foo{}bar".toByteArray()) % 16384
        slot("foo{}{bar}") shouldBe CRC16.lookup("foo{}{bar}".toByteArray()) % 16384
        slot("foo{}bar") shouldNotBe slot("foo")
    }

    @Test
    fun `unclosed or lone braces hash the whole key`() {
        slot("{") shouldBe CRC16.lookup("{".toByteArray()) % 16384
        slot("a{b") shouldBe CRC16.lookup("a{b".toByteArray()) % 16384
        slot("}a{") shouldBe CRC16.lookup("}a{".toByteArray()) % 16384
        slot("{}") shouldBe CRC16.lookup("{}".toByteArray()) % 16384
    }

    @Test
    fun `only the first brace pair counts`() {
        slot("a{b}c{d}") shouldBe slot("b")
        slot("{{a}}") shouldBe slot("{a")
        slot("{a}}") shouldBe slot("a")
    }

    @Test
    fun `binary keys are hashed by their bytes`() {
        val key = byteArrayOf(0x00, 0xFF.toByte(), '{'.code.toByte(), 0x80.toByte(), '}'.code.toByte(), 0x01)
        CRC16.slot(key) shouldBe CRC16.slot(byteArrayOf(0x80.toByte()))

        val noTag = byteArrayOf(0x00, 0xFF.toByte(), 0x7F)
        CRC16.slot(noTag) shouldBe CRC16.lookup(noTag) % 16384
    }

    @Test
    fun `slot is always in range`() {
        repeat(500) { slot("key$it") shouldBe slot("key$it") }
        (0 until 500).all { slot("key$it") in 0 until 16384 } shouldBe true
    }
}
