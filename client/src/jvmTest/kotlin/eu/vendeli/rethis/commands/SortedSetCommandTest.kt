package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.sortedset.*
import eu.vendeli.rethis.shared.request.sortedset.ZPopCommonOption
import eu.vendeli.rethis.shared.response.common.MPopResult
import eu.vendeli.rethis.shared.response.common.ScanResult
import eu.vendeli.rethis.shared.response.stream.ZMember
import eu.vendeli.rethis.shared.response.stream.ZPopResult
import eu.vendeli.rethis.shared.types.BulkString
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf

class SortedSetCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test BZMPOP command`() {
        client.zAdd("testSet1", ZMember("testValue1", 1.0))
        client.bzMPop(1.0, ZPopCommonOption.MIN, "testSet1", "testSet2") shouldBe listOf(
            MPopResult("testSet1", listOf()),
        )
    }

    @Test
    suspend fun `test BZPOPMAX command`() {
        client.zAdd("testSet3", ZMember("testValue3", 1.0))
        client.bzPopMax(1.0, "testSet3", "testSet4") shouldBe ZPopResult("testSet3", "testValue3", 1.0)
    }

    @Test
    suspend fun `test ZLEXCOUNT command`() {
        client.zAdd("testSet23", ZMember("testValue23", 1.0))
        client.zLexCount("testSet23", "-", "+") shouldBe 1L
    }

    @Test
    suspend fun `test ZMPOP command`() {
        client.zAdd("testSet24", ZMember("testValue24", 1.0))
        client.zMPop(ZPopCommonOption.MIN, "testSet24") shouldBe listOf(MPopResult("testSet24", listOf()))
    }

    @Test
    suspend fun `test ZMSCORE command`() {
        client.zAdd("testSet25", ZMember("testValue25", 1.0))
        client.zMScore("testSet25", "testValue25") shouldBe listOf(BulkString("1"))
    }

    @Test
    suspend fun `test ZPOPMAX command`() {
        client.zAdd("testSet26", ZMember("testValue26", 1.0))
        client.zPopMax("testSet26") shouldBe listOf(MPopResult("testValue26", listOf()))
    }

    @Test
    suspend fun `test ZPOPMIN command`() {
        client.zAdd("testSet27", ZMember("testValue27", 1.0))

        client.zPopMin("testSet27").shouldNotBeEmpty().first().shouldBeTypeOf<BulkString>().value shouldBe "testValue27"
    }

    @Test
    suspend fun `test ZPOPMIN command with count`() {
        client.zAdd("testSet27", ZMember("testValue27", 1.0))
        client.zPopMin("testSet27", 2) shouldBe listOf(BulkString("testValue27"), BulkString("1"))
    }

    @Test
    suspend fun `test ZRANDMEMBER command`() {
        client.zAdd("testSet28", ZMember("testValue28", 1.0))
        client.zRandMember("testSet28") shouldBe "testValue28"
    }

    @Test
    suspend fun `test ZRANDMEMBER command with count`() {
        client.zAdd("testSet28", ZMember("testValue28", 1.0))
        client.zRandMemberCount("testSet28", 1) shouldBe listOf("testValue28")
    }

    @Test
    suspend fun `test ZRANDMEMBER command with count + scores`() {
        client.zAdd("testSet28", ZMember("testValue28", 1.0))
        client.zRandMemberCount("testSet28", 1, true) shouldBe listOf("testValue28", "1")
    }

    @Test
    suspend fun `test ZRANGE command`() {
        client.zAdd("testSet29", ZMember("testValue29", 1.0))
        client.zRange("testSet29", "0", "-1") shouldBe listOf("testValue29")
    }

    @Test
    suspend fun `test ZRANGESTORE command`() {
        client.zAdd(
            "srczset",
            ZMember("one", 1.0),
            ZMember("two", 2.0),
            ZMember("three", 3.0),
            ZMember("four", 4.0),
        )

        client.zRangeStore("dstzset", "srczset", "2", "-1") shouldBe 2L
    }

    @Test
    suspend fun `test ZRANK command`() {
        client.zAdd("testSet32", ZMember("testValue32", 1.0))
        client.zRank("testSet32", "testValue32") shouldBe 0L
    }

    @Test
    suspend fun `test ZREM command`() {
        client.zAdd("testSet33", ZMember("testValue33", 1.0))
        client.zRem("testSet33", "testValue33") shouldBe 1L
    }

    @Test
    suspend fun `test ZREMRANGEBYLEX command`() {
        client.zAdd("testSet34", ZMember("testValue34", 1.0))

        client.zRemRangeByLex("testSet34", "-", "+") shouldBe 1L
    }

    @Test
    suspend fun `test ZREMRANGEBYRANK command`() {
        client.zAdd("testSet35", ZMember("testValue35", 1.0))
        client.zRemRangeByRank("testSet35", 0, -1) shouldBe 1L
    }

    @Test
    suspend fun `test ZREMRANGEBYSCORE command`() {
        client.zAdd("testSet36", ZMember("testValue36", 1.0))
        client.zRemRangeByScore("testSet36", 0.0, 2.0) shouldBe 1L
    }

    @Test
    suspend fun `test ZSCAN command`() {
        client.zAdd("testSet38", ZMember("testValue38", 1.0))
        client.zScan("testSet38", 0) shouldBe ScanResult("0", listOf("testValue38" to "1"))
    }

    @Test
    suspend fun `test ZSCORE command`() {
        client.zAdd("testSet39", ZMember("testValue39", 1.0))
        client.zScore("testSet39", "testValue39") shouldBe 1.0
    }

    @Test
    suspend fun `test ZUNION command`() {
        client.zAdd("testSet40", ZMember("testValue40", 1.0))
        client.zAdd("testSet41", ZMember("testValue41", 2.0))
        client.zUnion("testSet40", "testSet41") shouldBe listOf("testValue40", "testValue41")
    }

    @Test
    suspend fun `test ZUNIONSTORE command`() {
        client.zAdd("testSet42", ZMember("testValue42", 1.0))
        client.zAdd("testSet43", ZMember("testValue43", 2.0))
        client.zUnionStore(
            destination = "testSet44",
            key = arrayOf("testSet42", "testSet43"),
        ) shouldBe 2L
    }
}
