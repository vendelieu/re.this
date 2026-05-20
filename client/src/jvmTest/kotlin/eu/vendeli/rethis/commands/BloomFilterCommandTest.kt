package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.bf.bfAdd
import eu.vendeli.rethis.command.bf.bfCard
import eu.vendeli.rethis.command.bf.bfExists
import eu.vendeli.rethis.command.bf.bfMAdd
import eu.vendeli.rethis.command.bf.bfMExists
import eu.vendeli.rethis.command.bf.bfReserve
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class BloomFilterCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test BF_RESERVE and BF_ADD command`() {
        client.bfReserve("bfKey1", 0.01, 1000L, null, null) shouldBe "OK"
        client.bfAdd("bfKey1", "hello") shouldBe true
        client.bfExists("bfKey1", "hello") shouldBe true
        client.bfExists("bfKey1", "missing") shouldBe false
    }

    @Test
    suspend fun `test BF_CARD command`() {
        client.bfReserve("bfKey2", 0.01, 100L, null, null) shouldBe "OK"
        client.bfAdd("bfKey2", "a")
        client.bfAdd("bfKey2", "b")
        client.bfCard("bfKey2") shouldNotBe 0L
    }

    @Test
    suspend fun `test BF_MADD and BF_MEXISTS commands`() {
        client.bfReserve("bfKey3", 0.01, 100L, null, null) shouldBe "OK"
        client.bfMAdd("bfKey3", "x", "y", "z") shouldNotBe null
        client.bfMExists("bfKey3", "x", "missing") shouldNotBe null
    }
}
