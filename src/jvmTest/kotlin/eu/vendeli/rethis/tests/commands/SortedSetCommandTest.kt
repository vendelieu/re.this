package eu.vendeli.rethis.tests.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.*
import eu.vendeli.rethis.types.common.MPopResult
import eu.vendeli.rethis.types.common.ScanResult
import eu.vendeli.rethis.types.common.ZMember
import eu.vendeli.rethis.types.common.ZPopResult
import eu.vendeli.rethis.types.options.ZPopCommonOption
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SortedSetCommandTest : ReThisTestCtx() {
    @Test
    fun `test BZMPOP command`(): Unit = runTest {
        client.zAdd("testSet1", ZMember("testValue1", 1.0))
        client.bzMPop(1.0, ZPopCommonOption.MIN, "testSet1", "testSet2") shouldBe listOf(
            MPopResult("testSet1", listOf()),
        )
    }

    @Test
    fun `test BZPOPMAX command`(): Unit = runTest {
        client.zAdd("testSet3", ZMember("testValue3", 1.0))
        client.bzPopMax(1.0, "testSet3", "testSet4") shouldBe ZPopResult("testSet3", "testValue3", 1.0)
    }

    @Test
    fun `test ZLEXCOUNT command`(): Unit = runTest {
        client.zAdd("testSet23", ZMember("testValue23", 1.0))
        client.zLexCount("testSet23", "-", "+") shouldBe 1L
    }

    @Test
    fun `test ZMPOP command`(): Unit = runTest {
        client.zAdd("testSet24", ZMember("testValue24", 1.0))
        client.zMpop(ZPopCommonOption.MIN, "testSet24") shouldBe listOf(MPopResult("testSet24", listOf()))
    }

    @Test
    fun `test ZMSCORE command`(): Unit = runTest {
        client.zAdd("testSet25", ZMember("testValue25", 1.0))
        client.zMscore("testSet25", "testValue25") shouldBe listOf(1.0)
    }

    @Test
    fun `test ZPOPMAX command`(): Unit = runTest {
        client.zAdd("testSet26", ZMember("testValue26", 1.0))
        client.zPopmax("testSet26") shouldBe listOf(MPopResult("testValue26", listOf()))
    }

    @Test
    fun `test ZPOPMIN command`(): Unit = runTest {
        client.zAdd("testSet27", ZMember("testValue27", 1.0))

        client.zPopmin("testSet27") shouldBe listOf("testValue27", 1.0)
    }

    @Test
    fun `test ZPOPMIN command with count`(): Unit = runTest {
        client.zAdd("testSet27", ZMember("testValue27", 1.0))
        client.zPopmin("testSet27", 2) shouldBe listOf(listOf(ZMember("testValue27", 1.0)))
    }

    @Test
    fun `test ZRANDMEMBER command`(): Unit = runTest {
        client.zAdd("testSet28", ZMember("testValue28", 1.0))
        client.zRandmember("testSet28") shouldBe "testValue28"
    }

    @Test
    fun `test ZRANDMEMBER command with count`(): Unit = runTest {
        client.zAdd("testSet28", ZMember("testValue28", 1.0))
        client.zRandmember("testSet28", 1) shouldBe listOf("testValue28")
    }

    @Test
    fun `test ZRANDMEMBER command with count + scores`(): Unit = runTest {
        client.zAdd("testSet28", ZMember("testValue28", 1.0))
        client.zRandmember("testSet28", 1, true) shouldBe listOf(listOf(ZMember("testValue28", 1.0)))
    }

    @Test
    fun `test ZRANGE command`(): Unit = runTest {
        client.zAdd("testSet29", ZMember("testValue29", 1.0))
        client.zRange("testSet29", 0, -1) shouldBe listOf("testValue29")
    }

    @Test
    fun `test ZRANGESTORE command`(): Unit = runTest {
        client.zAdd(
            "srczset",
            ZMember("one", 1.0),
            ZMember("two", 2.0),
            ZMember("three", 3.0),
            ZMember("four", 4.0),
        )

        client.zRangeStore("dstzset", "srczset", 2, -1) shouldBe 2L
    }

    @Test
    fun `test ZRANK command`(): Unit = runTest {
        client.zAdd("testSet32", ZMember("testValue32", 1.0))
        client.zRank("testSet32", "testValue32") shouldBe 0L
    }

    @Test
    fun `test ZREM command`(): Unit = runTest {
        client.zAdd("testSet33", ZMember("testValue33", 1.0))
        client.zRem("testSet33", "testValue33") shouldBe 1L
    }

    @Test
    fun `test ZREMRANGEBYLEX command`(): Unit = runTest {
        client.zAdd("testSet34", ZMember("testValue34", 1.0))

        client.zRemRangeByLex("testSet34", "-", "+") shouldBe 1L
    }

    @Test
    fun `test ZREMRANGEBYRANK command`(): Unit = runTest {
        client.zAdd("testSet35", ZMember("testValue35", 1.0))
        client.zRemRangeByRank("testSet35", 0, -1) shouldBe 1L
    }

    @Test
    fun `test ZREMRANGEBYSCORE command`(): Unit = runTest {
        client.zAdd("testSet36", ZMember("testValue36", 1.0))
        client.zRemRangeByScore("testSet36", 0.0, 2.0) shouldBe 1L
    }

    @Test
    fun `test ZSCAN command`(): Unit = runTest {
        client.zAdd("testSet38", ZMember("testValue38", 1.0))
        client.zScan("testSet38", 0) shouldBe ScanResult("0", listOf("testValue38" to "1"))
    }

    @Test
    fun `test ZSCORE command`(): Unit = runTest {
        client.zAdd("testSet39", ZMember("testValue39", 1.0))
        client.zScore("testSet39", "testValue39") shouldBe 1.0
    }

    @Test
    fun `test ZUNION command`(): Unit = runTest {
        client.zAdd("testSet40", ZMember("testValue40", 1.0))
        client.zAdd("testSet41", ZMember("testValue41", 2.0))
        client.zUnion("testSet40", "testSet41") shouldBe listOf("testValue40", "testValue41")
    }

    @Test
    fun `test ZUNIONSTORE command`(): Unit = runTest {
        client.zAdd("testSet42", ZMember("testValue42", 1.0))
        client.zAdd("testSet43", ZMember("testValue43", 2.0))
        client.zUnionStore("testSet44", "testSet42", "testSet43") shouldBe 2L
    }
}
