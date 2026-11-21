package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.codecs.sortedset.ZPopMaxCommandCodec
import eu.vendeli.rethis.codecs.sortedset.ZRemRangeByScoreCommandCodec
import eu.vendeli.rethis.command.sortedset.*
import eu.vendeli.rethis.safeCast
import eu.vendeli.rethis.shared.request.sortedset.ZPopCommonOption
import eu.vendeli.rethis.shared.response.common.MPopResult
import eu.vendeli.rethis.shared.response.common.ScanResult
import eu.vendeli.rethis.shared.response.sortedset.ZMember
import eu.vendeli.rethis.shared.response.sortedset.ZPopResult
import eu.vendeli.rethis.shared.types.BulkString
import eu.vendeli.rethis.shared.types.F64
import eu.vendeli.rethis.shared.types.RArray
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.utils.toRespDouble
import eu.vendeli.rethis.shared.utils.unwrap
import eu.vendeli.rethis.types.common.RespVer
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.io.Buffer
import kotlinx.io.readString
import kotlin.math.roundToInt

class SortedSetCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test BZMPOP command`() {
        client.zAdd("testSet1", ZMember("testValue1", 1.0))
        client.bzMPop(1.0, ZPopCommonOption.MIN, "testSet1", "testSet2") shouldBe listOf(
            MPopResult("testSet1", listOf("testValue1", "1.0")),
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
        client.zMPop(ZPopCommonOption.MIN, "testSet24") shouldBe
            listOf(MPopResult("testSet24", listOf("testValue24", "1.0")))
    }

    @Test
    suspend fun `test ZMSCORE command`() {
        client.zAdd("testSet25", ZMember("testValue25", 1.0))
        client.zMScore("testSet25", "testValue25").shouldNotBeNull().first().also {
            val score = if (it is BulkString) {
                it.value.readString()
            } else {
                (it as F64).value.roundToInt().toString()
            }
            score shouldBe "1"
        }
    }

    @Test
    suspend fun `test ZPOPMAX command`() {
        client.zAdd("testSet26", ZMember("testValue26", 1.0))
        client.zPopMax("testSet26").memberScores() shouldBe listOf("testValue26" to 1.0)
    }

    @Test
    suspend fun `test ZPOPMAX command with count`() {
        client.zAdd("testSet26c", ZMember("a", 1.0), ZMember("b", 2.0), ZMember("c", 3.0))
        client.zPopMax("testSet26c", 2).memberScores() shouldBe listOf("c" to 3.0, "b" to 2.0)
    }

    @Test
    suspend fun `test ZPOPMAX command with count over RESP2`() {
        val resp2 = createClient(protocol = RespVer.V2)
        try {
            resp2.zAdd("testSet26r2", ZMember("a", 1.0), ZMember("b", 2.0))
            resp2.zPopMax("testSet26r2", 2).memberScores() shouldBe listOf("b" to 2.0, "a" to 1.0)
        } finally {
            resp2.close()
        }
    }

    @Test
    suspend fun `test ZPOPMAX codec decodes flat RESP2 member and score pairs`() {
        val reply = Buffer().also {
            it.write("*4\r\n\$2\r\nm1\r\n\$1\r\n1\r\n\$2\r\nm2\r\n\$1\r\n2\r\n".encodeToByteArray())
        }

        ZPopMaxCommandCodec.decode(reply, defaultCharset).memberScores() shouldBe listOf("m1" to 1.0, "m2" to 2.0)
    }

    @Test
    suspend fun `test ZPOPMAX codec decodes nested RESP3 member and score pairs`() {
        val reply = Buffer().also {
            it.write("*2\r\n*2\r\n\$2\r\nm1\r\n,1\r\n*2\r\n\$2\r\nm2\r\n,2\r\n".encodeToByteArray())
        }

        ZPopMaxCommandCodec.decode(reply, defaultCharset).memberScores() shouldBe listOf("m1" to 1.0, "m2" to 2.0)
    }

    private fun List<RType>.memberScores(): List<Pair<String, Double>> {
        val pairs = if (firstOrNull() is RArray) map { (it as RArray).value } else chunked(2)
        return pairs.map { (member, score) ->
            member.unwrap<String>()!! to (score.unwrap<Double>() ?: score.unwrap<String>()!!.toRespDouble())
        }
    }

    @Test
    suspend fun `test ZPOPMIN command`() {
        client.zAdd("testSet27", ZMember("testValue27", 1.0))

        client
            .zPopMin("testSet27")
            .shouldNotBeEmpty()
            .first()
            .shouldBeTypeOf<BulkString>()
            .value
            .readString() shouldBe "testValue27"
    }

    @Test
    suspend fun `test ZPOPMIN command with count`() {
        client.zAdd("testSet27", ZMember("testValue27", 1.0))
        val data = client.zPopMin("testSet27", 2)
        val result = if (data.first() is BulkString) {
            data
        } else {
            data.first().shouldBeTypeOf<RArray>().value
        }

        result shouldHaveSize 2
        result
            .first()
            .safeCast<BulkString>()
            ?.value
            ?.readString() shouldBe "testValue27"
        result.last().let {
            if (it is BulkString) {
                it.value.readString()
            } else {
                (it as F64).value.roundToInt().toString()
            }
        } shouldBe "1"
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
        val result = client
            .zRandMemberWithScores("testSet28", 1, true)
            .first() // unwrapping array
            .shouldBeTypeOf<RArray>()
            .value
        result
            .first()
            .shouldBeTypeOf<BulkString>()
            .value
            .readString() shouldBe "testValue28"
        result.last().shouldBeTypeOf<F64>().value shouldBe 1.0
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
        client.zRemRangeByScore("testSet36", "0", "2") shouldBe 1L
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

    @Test
    suspend fun `test ZREMRANGEBYSCORE command with exclusive bounds`() {
        client.zAdd("testSetRemBounds", *(1..5).map { ZMember("m$it", it.toDouble()) }.toTypedArray())

        client.zRemRangeByScore("testSetRemBounds", "(1", "(5") shouldBe 3L
        client.zCard("testSetRemBounds") shouldBe 2L
    }

    @Test
    suspend fun `test ZREMRANGEBYSCORE codec writes bounds verbatim`() {
        ZRemRangeByScoreCommandCodec.encode(defaultCharset, "k", "(1", "-inf").data.readString() shouldBe
            "*4\r\n\$16\r\nZREMRANGEBYSCORE\r\n\$1\r\nk\r\n\$2\r\n(1\r\n\$4\r\n-inf\r\n"
    }

    @Test
    suspend fun `test ZRANDMEMBER returns null for a missing key on RESP3`() {
        client.zRandMember("missingZRandKey") shouldBe null
        client.zRandMemberBA("missingZRandKey") shouldBe null
    }

    @Test
    suspend fun `test ZRANDMEMBER returns null for a missing key on RESP2`() {
        val resp2 = createClient(protocol = RespVer.V2)
        try {
            resp2.zRandMember("missingZRandKey2") shouldBe null
            resp2.zRandMemberBA("missingZRandKey2") shouldBe null
        } finally {
            resp2.close()
        }
    }

    @Test
    suspend fun `test BZPOPMIN and BZPOPMAX return null on timeout on RESP3`() {
        client.bzPopMin(0.1, "missingBzKey") shouldBe null
        client.bzPopMax(0.1, "missingBzKey") shouldBe null
    }

    @Test
    suspend fun `test BZPOPMIN and BZPOPMAX return null on timeout on RESP2`() {
        val resp2 = createClient(protocol = RespVer.V2)
        try {
            resp2.bzPopMin(0.1, "missingBzKey2") shouldBe null
            resp2.bzPopMax(0.1, "missingBzKey2") shouldBe null
        } finally {
            resp2.close()
        }
    }

    @Test
    suspend fun `test ZMPOP and BZMPOP return null when no element is available on RESP3`() {
        client.zMPop(ZPopCommonOption.MIN, "missingZMPopKey") shouldBe null
        client.bzMPop(0.1, ZPopCommonOption.MIN, "missingZMPopKey") shouldBe null
    }

    @Test
    suspend fun `test ZMPOP and BZMPOP return null when no element is available on RESP2`() {
        val resp2 = createClient(protocol = RespVer.V2)
        try {
            resp2.zMPop(ZPopCommonOption.MIN, "missingZMPopKey2") shouldBe null
            resp2.bzMPop(0.1, ZPopCommonOption.MIN, "missingZMPopKey2") shouldBe null
        } finally {
            resp2.close()
        }
    }
}
