package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.bitmap.*
import eu.vendeli.rethis.shared.request.bitmap.BitOpOption
import eu.vendeli.rethis.shared.request.bitmap.BitfieldOption
import io.kotest.matchers.shouldBe

class BitmapCommandsTest : ReThisTestCtx() {
    @Test
    suspend fun `test BITCOUNT command`() {
        val key = "testBitCountKey1"
        client.setBit(key, 0, 1)
        client.setBit(key, 1, 1)
        client.setBit(key, 2, 0)

        client.bitCount(key) shouldBe 2L
    }

    @Test
    suspend fun `test BITFIELD command`() {
        val key = "testBitFieldKey2"
        client.setBit(key, 0, 1)
        client.setBit(key, 1, 1)
        client.setBit(key, 2, 0)

        val result = client.bitfield(key, BitfieldOption.Set("i5", 0, 1), BitfieldOption.Get("i5", 0))
        result shouldBe listOf(-8L, 1L)
    }

    @Test
    suspend fun `test BITFIELD_RO command`() {
        val key = "testBitFieldROKey3"
        client.setBit(key, 0, 1)
        client.setBit(key, 1, 1)

        val result = client.bitfieldRo(key, BitfieldOption.Get("i5", 0))
        result shouldBe listOf(-8L)
    }

    @Test
    suspend fun `test BITOP command`() {
        val key1 = "testBitOpKey4"
        val key2 = "testBitOpKey5"
        val destKey = "testBitOpDestKey4"
        client.setBit(key1, 0, 1)
        client.setBit(key1, 1, 1)
        client.setBit(key2, 0, 1)

        client.bitOp(BitOpOption.OperationType.AND, destKey, key1, key2) shouldBe 1L
        client.bitCount(destKey) shouldBe 1L
    }

    @Test
    suspend fun `test BITPOS command`() {
        val key = "testBitPosKey5"
        client.setBit(key, 0, 1)
        client.setBit(key, 1, 1)
        client.setBit(key, 2, 0)

        client.bitPos(key, 1) shouldBe 0L
    }

    @Test
    suspend fun `test GETBIT command`() {
        val key = "testGetBitKey6"
        client.setBit(key, 0, 1)

        client.getBit(key, 0) shouldBe 1L
        client.getBit(key, 1) shouldBe 0L
    }

    @Test
    suspend fun `test SETBIT command`() {
        val key = "testSetBitKey7"
        client.setBit(key, 0, 1) shouldBe 0L
        client.setBit(key, 0, 0) shouldBe 1L
    }
}
