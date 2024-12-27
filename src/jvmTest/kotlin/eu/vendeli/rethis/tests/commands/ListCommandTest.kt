package eu.vendeli.rethis.tests.commands

import eu.vendeli.rethis.commands.*
import eu.vendeli.rethis.types.common.MPopResult
import eu.vendeli.rethis.types.common.PopResult
import eu.vendeli.rethis.types.options.LInsertPlace
import eu.vendeli.rethis.types.common.MoveDirection
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class ListCommandTest : ReThisTestCtx() {
    @Test
    fun `test BLMOVE command`(): Unit = runBlocking {
        client.lPush("testKey1", "testValue1")
        client.blMove("testKey1", "testKey2", MoveDirection.LEFT, MoveDirection.LEFT, 10) shouldBe "testValue1"
    }

    @Test
    fun `test BLMPOP command`(): Unit = runBlocking {
        client.lPush("testKey3", "testValue3")
        client.blmPop(10, "testKey3", direction = MoveDirection.LEFT) shouldBe listOf(
            MPopResult(
                "testKey3",
                listOf("testValue3"),
            ),
        )
    }

    @Test
    fun `test BLPOP command`(): Unit = runBlocking {
        client.lPush("testKey4", "testValue4")
        client.blPop("testKey4") shouldBe PopResult("testKey4", "testValue4")
    }

    @Test
    fun `test BRPOP command`(): Unit = runBlocking {
        client.rPush("testKey5", "testValue5")
        client.brPop(10, "testKey5") shouldBe PopResult("testKey5", "testValue5")
    }

    @Test
    fun `test LINDEX command`(): Unit = runBlocking {
        client.lPush("testKey6", "testValue6")
        client.lIndex("testKey6", 0) shouldBe "testValue6"
    }

    @Test
    fun `test LINSERT command`(): Unit = runBlocking {
        client.lPush("testKey7", "testValue7")
        client.lInsert("testKey7", LInsertPlace.BEFORE, "testValue7", "newValue7") shouldBe 2L
    }

    @Test
    fun `test LLEN command`(): Unit = runBlocking {
        client.lPush("testKey8", "testValue8")
        client.lLen("testKey8") shouldBe 1L
    }

    @Test
    fun `test LMOVE command`(): Unit = runBlocking {
        client.lPush("testKey9", "testValue9")
        client.lMove("testKey9", "testKey10", MoveDirection.LEFT, MoveDirection.LEFT) shouldBe "testValue9"
    }

    @Test
    fun `test LMPop command`(): Unit = runBlocking {
        client.lPush("testKey11", "testValue11")
        client.lmPop(MoveDirection.LEFT, "testKey11") shouldBe listOf(
            MPopResult(
                "testKey11",
                listOf("testValue11"),
            ),
        )
    }

    @Test
    fun `test LPOP command`(): Unit = runBlocking {
        client.lPush("testKey12", "testValue12")
        client.lPop("testKey12") shouldBe "testValue12"
    }

    @Test
    fun `test LPOP command with count`(): Unit = runBlocking {
        client.lPush("testKey13", "testValue13")
        client.lPush("testKey13", "testValue14")
        client.lPop("testKey13", 2) shouldBe listOf("testValue14", "testValue13")
    }

    @Test
    fun `test LPOS command`(): Unit = runBlocking {
        client.lPush("testKey15", "testValue15")
        client.lPos("testKey15", "testValue15") shouldBe 0L
    }

    @Test
    fun `test RPUSH command`(): Unit = runBlocking {
        client.rPush("testKey24", "testValue24") shouldBe 1L
    }

    @Test
    fun `test RPUSHX command`(): Unit = runBlocking {
        client.lPush("testKey25", "testValue52")

        client.rPushX("testKey25", "testValue25") shouldBe 2L
    }
}
