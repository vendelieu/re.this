package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.topk.topkAdd
import eu.vendeli.rethis.command.topk.topkQuery
import eu.vendeli.rethis.command.topk.topkReserve
import eu.vendeli.rethis.shared.types.Bool
import eu.vendeli.rethis.shared.types.RArray
import eu.vendeli.rethis.shared.types.RType
import io.kotest.matchers.shouldBe

class TopKCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test TOPK_RESERVE and TOPK_ADD commands`() {
        client.topkReserve("topkKey1", 5L, null) shouldBe "OK"
        // nothing is expelled from a top-k with free slots
        client.topkAdd("topkKey1", "a", "b", "c") shouldBe RArray(listOf(RType.Null, RType.Null, RType.Null))
        client.topkQuery("topkKey1", "a", "b", "c", "missing") shouldBe
            RArray(listOf(Bool(true), Bool(true), Bool(true), Bool(false)))
    }

    @Test
    suspend fun `test TOPK_QUERY command`() {
        client.topkReserve("topkKey2", 5L, null) shouldBe "OK"
        client.topkAdd("topkKey2", "x", "y")
        client.topkQuery("topkKey2", "x", "missing") shouldBe RArray(listOf(Bool(true), Bool(false)))
    }
}
