package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.list.*
import eu.vendeli.rethis.shared.request.list.LInsertPlace
import eu.vendeli.rethis.shared.response.common.MPopResult
import eu.vendeli.rethis.shared.response.common.MoveDirection
import eu.vendeli.rethis.shared.response.common.PopResult
import eu.vendeli.rethis.types.common.RespVer
import io.kotest.matchers.shouldBe

class ListCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test BLMOVE command`() {
        client.lPush("testKey1", "testValue1")
        client.blMove("testKey1", "testKey2", MoveDirection.LEFT, MoveDirection.LEFT, 10.0) shouldBe "testValue1"
    }

    @Test
    suspend fun `test BLMPOP command`() {
        client.lPush("testKey3", "testValue3")
        client.blmPop(10.0, "testKey3", where = MoveDirection.LEFT) shouldBe listOf(
            MPopResult(
                "testKey3",
                listOf("testValue3"),
            ),
        )
    }

    @Test
    suspend fun `test BLPOP command`() {
        client.lPush("testKey4", "testValue4")
        client.blPop("testKey4", timeout = 0.0) shouldBe PopResult("testKey4", "testValue4")
    }

    @Test
    suspend fun `test BRPOP command`() {
        client.rPush("testKey5", "testValue5")
        client.brPop("testKey5", timeout = 10.0) shouldBe PopResult("testKey5", "testValue5")
    }

    @Test
    suspend fun `test LINDEX command`() {
        client.lPush("testKey6", "testValue6")
        client.lIndex("testKey6", 0) shouldBe "testValue6"
    }

    @Test
    suspend fun `test LINSERT command`() {
        client.lPush("testKey7", "testValue7")
        client.lInsert("testKey7", LInsertPlace.BEFORE, "testValue7", "newValue7") shouldBe 2L
    }

    @Test
    suspend fun `test LLEN command`() {
        client.lPush("testKey8", "testValue8")
        client.lLen("testKey8") shouldBe 1L
    }

    @Test
    suspend fun `test LMOVE command`() {
        client.lPush("testKey9", "testValue9")
        client.lMove("testKey9", "testKey10", MoveDirection.LEFT, MoveDirection.LEFT) shouldBe "testValue9"
    }

    @Test
    suspend fun `test LMPop command`() {
        client.lPush("testKey11", "testValue11")
        client.lmPop(MoveDirection.LEFT, "testKey11") shouldBe listOf(
            MPopResult(
                "testKey11",
                listOf("testValue11"),
            ),
        )
    }

    @Test
    suspend fun `test LPOP command`() {
        client.lPush("testKey12", "testValue12")
        client.lPop("testKey12") shouldBe "testValue12"
    }

    @Test
    suspend fun `test LPOP command with count`() {
        client.lPush("testKey13", "testValue13")
        client.lPush("testKey13", "testValue14")
        client.lPopCount("testKey13", 2) shouldBe listOf("testValue14", "testValue13")
    }

    @Test
    suspend fun `test LPOS command`() {
        client.lPush("testKey15", "testValue15")
        client.lPos("testKey15", "testValue15") shouldBe 0L
    }

    @Test
    suspend fun `test RPUSH command`() {
        client.rPush("testKey24", "testValue24") shouldBe 1L
    }

    @Test
    suspend fun `test RPUSHX command`() {
        client.lPush("testKey25", "testValue52")

        client.rPushx("testKey25", "testValue25") shouldBe 2L
    }

    @Test
    suspend fun `test LMOVE returns null for a missing source on RESP3`() {
        client.lMove("missingMoveSrc", "moveDst", MoveDirection.LEFT, MoveDirection.RIGHT) shouldBe null
        client.lMoveBA("missingMoveSrc", "moveDst", MoveDirection.LEFT, MoveDirection.RIGHT) shouldBe null
    }

    @Test
    suspend fun `test LMOVE returns null for a missing source on RESP2`() {
        val resp2 = createClient(protocol = RespVer.V2)
        try {
            resp2.lMove("missingMoveSrc2", "moveDst", MoveDirection.LEFT, MoveDirection.RIGHT) shouldBe null
            resp2.lMoveBA("missingMoveSrc2", "moveDst", MoveDirection.LEFT, MoveDirection.RIGHT) shouldBe null
        } finally {
            resp2.close()
        }
    }

    @Test
    suspend fun `test LPOP and RPOP with count on a missing key on RESP3`() {
        client.lPopCount("missingPopKey", 2).orEmpty() shouldBe emptyList()
        client.rPopCount("missingPopKey", 2).orEmpty() shouldBe emptyList()
    }

    @Test
    suspend fun `test LPOP and RPOP with count on a missing key on RESP2`() {
        val resp2 = createClient(protocol = RespVer.V2)
        try {
            resp2.lPopCount("missingPopKey2", 2).orEmpty() shouldBe emptyList()
            resp2.rPopCount("missingPopKey2", 2).orEmpty() shouldBe emptyList()
        } finally {
            resp2.close()
        }
    }

    @Test
    suspend fun `test BLPOP and BRPOP return null on timeout on RESP3`() {
        client.blPop("missingBlKey", timeout = 0.1) shouldBe null
        client.brPop("missingBlKey", timeout = 0.1) shouldBe null
    }

    @Test
    suspend fun `test BLPOP and BRPOP return null on timeout on RESP2`() {
        val resp2 = createClient(protocol = RespVer.V2)
        try {
            resp2.blPop("missingBlKey2", timeout = 0.1) shouldBe null
            resp2.brPop("missingBlKey2", timeout = 0.1) shouldBe null
        } finally {
            resp2.close()
        }
    }

    @Test
    suspend fun `test LMPOP and BLMPOP return null when no element is available on RESP3`() {
        client.lmPop(MoveDirection.LEFT, "missingMPopKey") shouldBe null
        client.blmPop(0.1, "missingMPopKey", where = MoveDirection.LEFT) shouldBe null
    }

    @Test
    suspend fun `test LMPOP and BLMPOP return null when no element is available on RESP2`() {
        val resp2 = createClient(protocol = RespVer.V2)
        try {
            resp2.lmPop(MoveDirection.LEFT, "missingMPopKey2") shouldBe null
            resp2.blmPop(0.1, "missingMPopKey2", where = MoveDirection.LEFT) shouldBe null
        } finally {
            resp2.close()
        }
    }
}
