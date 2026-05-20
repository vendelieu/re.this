package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.topk.topkAdd
import eu.vendeli.rethis.command.topk.topkQuery
import eu.vendeli.rethis.command.topk.topkReserve
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class TopKCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test TOPK_RESERVE and TOPK_ADD commands`() {
        client.topkReserve("topkKey1", 5L, null) shouldBe "OK"
        client.topkAdd("topkKey1", "a", "b", "c") shouldNotBe null
    }

    @Test
    suspend fun `test TOPK_QUERY command`() {
        client.topkReserve("topkKey2", 5L, null) shouldBe "OK"
        client.topkAdd("topkKey2", "x", "y")
        client.topkQuery("topkKey2", "x", "missing") shouldNotBe null
    }
}
