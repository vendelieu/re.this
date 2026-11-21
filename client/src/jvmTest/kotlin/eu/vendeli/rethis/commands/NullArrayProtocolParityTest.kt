package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.list.blPop
import eu.vendeli.rethis.command.list.lPopCount
import eu.vendeli.rethis.command.list.lmPop
import eu.vendeli.rethis.command.list.rPopCount
import eu.vendeli.rethis.command.sortedset.zAdd
import eu.vendeli.rethis.command.sortedset.zRankWithScores
import eu.vendeli.rethis.shared.response.common.MoveDirection
import eu.vendeli.rethis.shared.response.sortedset.ZMember
import eu.vendeli.rethis.types.common.RespVer
import io.kotest.matchers.shouldBe

// Specs that declare NULL next to ARRAY must report an absent result as null on both protocols:
// RESP3 sends `_`, RESP2 sends the null array `*-1`.
class NullArrayProtocolParityTest : ReThisTestCtx() {
    private suspend fun onResp2(block: suspend (ReThis) -> Unit) {
        val resp2 = createClient(protocol = RespVer.V2)
        try {
            block(resp2)
        } finally {
            resp2.close()
        }
    }

    private suspend fun ReThis.assertAbsentResultsAreNull() {
        lPopCount("nullArrayMissingList", 2) shouldBe null
        rPopCount("nullArrayMissingList", 2) shouldBe null
        zAdd("nullArrayZset", ZMember("present", 1.0))
        zRankWithScores("nullArrayZset", "absent", true) shouldBe null
    }

    @Test
    suspend fun `absent array results are null on RESP3`() = client.assertAbsentResultsAreNull()

    @Test
    suspend fun `absent array results are null on RESP2`() = onResp2 { it.assertAbsentResultsAreNull() }

    // BLPOP / LMPOP go through dedicated decoders that report an absent result the same way on both protocols.
    private suspend fun ReThis.absentPopResults() = blPop("nullArrayMissingList", timeout = 0.1) to
        lmPop(MoveDirection.LEFT, "nullArrayMissingList")

    @Test
    suspend fun `blocking and multi pops report an absent result alike on both protocols`() {
        val resp3 = client.absentPopResults()
        onResp2 { it.absentPopResults() shouldBe resp3 }
    }
}
