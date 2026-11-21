package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.codecs.sortedset.ZCountCommandCodec
import eu.vendeli.rethis.codecs.sortedset.ZIncrByCommandCodec
import eu.vendeli.rethis.codecs.sortedset.ZInterCommandCodec
import eu.vendeli.rethis.codecs.sortedset.ZInterStoreCommandCodec
import eu.vendeli.rethis.codecs.sortedset.ZRevRankWithScoreCommandCodec
import eu.vendeli.rethis.codecs.sortedset.ZUnionCommandCodec
import eu.vendeli.rethis.codecs.sortedset.ZUnionStoreCommandCodec
import eu.vendeli.rethis.command.sortedset.*
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.utils.toRespDouble
import eu.vendeli.rethis.shared.utils.unwrap
import eu.vendeli.rethis.types.common.RespVer
import kotlinx.io.Buffer
import kotlinx.io.readString
import eu.vendeli.rethis.shared.response.sortedset.ZMember
import eu.vendeli.rethis.shared.response.sortedset.ZPopResult
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class SortedSetCommandTest2 : ReThisTestCtx() {
    @Test
    suspend fun `test BZPOPMIN command`() {
        client.zAdd("testSet5", ZMember("testValue5", 1.0))

        client.bzPopMin(1.0, "testSet5", "testSet6") shouldBe ZPopResult("testSet5", "testValue5", 1.0)
    }

    @Test
    suspend fun `test ZADD command`() {
        client.zAdd("testSet7", ZMember("testValue7", 1.0)) shouldBe 1L
    }

    @Test
    suspend fun `test ZREVRANK command`() {
        client.zAdd("testSet37", ZMember("testValue37", 1.0))

        client.zRevRank("testSet37", "testValue37") shouldBe 0L
    }

    @Test
    suspend fun `test ZCARD command`() {
        client.zAdd("testSet8", ZMember("testValue8", 1.0))

        client.zCard("testSet8") shouldBe 1L
    }

    @Test
    suspend fun `test ZCOUNT command`() {
        client.zAdd("testSet9", ZMember("testValue9", 1.0))

        client.zCount("testSet9", "0", "2") shouldBe 1L
    }

    @Test
    suspend fun `test ZDIFF command`() {
        client.zAdd("testSet10", ZMember("testValue10", 1.0))
        client.zAdd("testSet11", ZMember("testValue11", 2.0))

        client.zDiff("testSet10", "testSet11") shouldBe listOf("testValue10")
    }

    @Test
    suspend fun `test ZDIFFSTORE command`() {
        client.zAdd("testSet12", ZMember("testValue12", 1.0))
        client.zAdd("testSet13", ZMember("testValue13", 2.0))

        client.zDiffStore("testSet14", "testSet12", "testSet13") shouldBe 1L
    }

    @Test
    suspend fun `test ZINCRBY command`() {
        client.zAdd("testSet15", ZMember("testValue15", 1.0))
        client.zIncrBy("testSet15", "testValue15", 1.0) shouldBe 2.0
    }

    @Test
    suspend fun `ZINCRBY accepts infinite increments`() {
        client.zIncrBy("testSetIncrInf", "up", Double.POSITIVE_INFINITY) shouldBe Double.POSITIVE_INFINITY
        client.zIncrBy("testSetIncrInf", "down", Double.NEGATIVE_INFINITY) shouldBe Double.NEGATIVE_INFINITY
        client.zIncrBy("testSetIncrInf", "up", -5.0) shouldBe Double.POSITIVE_INFINITY
        client.zScore("testSetIncrInf", "down") shouldBe Double.NEGATIVE_INFINITY
    }

    @Test
    suspend fun `test ZINTER command`() {
        client.zAdd("testSet16", ZMember("testValue16", 1.0))
        client.zAdd("testSet17", ZMember("testValue16", 2.0))

        client.zInter("testSet16", "testSet17") shouldBe listOf("testValue16")
    }

    @Test
    suspend fun `test ZINTERCARD command`() {
        client.zAdd("testSet18", ZMember("testValue18", 1.0))
        client.zAdd("testSet19", ZMember("testValue18", 2.0))

        client.zInterCard("testSet18", "testSet19") shouldBe 1L
    }

    @Test
    suspend fun `test ZINTERSTORE command`() {
        client.zAdd("testSet20", ZMember("testValue20", 1.0))
        client.zAdd("testSet21", ZMember("testValue20", 2.0))

        client.zInterStore("testSet22", key = arrayOf("testSet20", "testSet21")) shouldBe 1L
    }

    private fun List<RType>?.rankAndScore(): Pair<Long, Double> {
        val reply = requireNotNull(this)
        val score = reply[1].unwrap<Double>() ?: reply[1].unwrap<String>()!!.toRespDouble()
        return reply[0].unwrap<Long>()!! to score
    }

    @Test
    suspend fun `test ZREVRANK WITHSCORE command with RESP3`() {
        client.zAdd("testSetRevRankScore3", ZMember("m", 1.5))

        client.zRevRankWithScore("testSetRevRankScore3", "m", true).rankAndScore() shouldBe (0L to 1.5)
    }

    @Test
    suspend fun `test ZREVRANK WITHSCORE command with RESP2`() {
        val resp2 = createClient(protocol = RespVer.V2)
        try {
            resp2.zAdd("testSetRevRankScore2", ZMember("m", 1.5))

            resp2.zRevRankWithScore("testSetRevRankScore2", "m", true).rankAndScore() shouldBe (0L to 1.5)
        } finally {
            resp2.close()
        }
    }

    @Test
    suspend fun `test ZREVRANK WITHSCORE codec sends token only when requested and a correct header otherwise`() {
        ZRevRankWithScoreCommandCodec.encode(defaultCharset, "k", "m", true).data.readString() shouldBe
            "*4\r\n\$8\r\nZREVRANK\r\n\$1\r\nk\r\n\$1\r\nm\r\n\$9\r\nWITHSCORE\r\n"
        ZRevRankWithScoreCommandCodec.encode(defaultCharset, "k", "m", false).data.readString() shouldBe
            "*3\r\n\$8\r\nZREVRANK\r\n\$1\r\nk\r\n\$1\r\nm\r\n"
    }

    @Test
    suspend fun `test ZREVRANK WITHSCORE codec decodes a fractional RESP2 score`() {
        val reply = Buffer().also { it.write("*2\r\n:0\r\n\$3\r\n1.5\r\n".encodeToByteArray()) }

        ZRevRankWithScoreCommandCodec.decode(reply, defaultCharset).rankAndScore() shouldBe (0L to 1.5)
    }

    @Test
    suspend fun `test ZINCRBY command with a fractional increment`() {
        client.zAdd("testSetIncrFrac", ZMember("m", 1.0))

        client.zIncrBy("testSetIncrFrac", "m", 0.5) shouldBe 1.5
    }

    @Test
    suspend fun `test ZINCRBY codec writes a fractional increment`() {
        ZIncrByCommandCodec.encode(defaultCharset, "k", "m", 0.5).data.readString() shouldBe
            "*4\r\n\$7\r\nZINCRBY\r\n\$1\r\nk\r\n\$3\r\n0.5\r\n\$1\r\nm\r\n"
    }

    private suspend fun seedWeightedSets(prefix: String) {
        client.zAdd("${prefix}a", ZMember("x", 1.0), ZMember("y", 3.0))
        client.zAdd("${prefix}b", ZMember("x", 3.0), ZMember("y", 1.0))
    }

    @Test
    suspend fun `test ZINTER command with fractional weights`() {
        seedWeightedSets("testSetInterW")

        client.zInter("testSetInterWa", "testSetInterWb", weight = listOf(0.5, 2.0)) shouldBe listOf("y", "x")
    }

    @Test
    suspend fun `test ZUNION command with fractional weights`() {
        seedWeightedSets("testSetUnionW")

        client.zUnion("testSetUnionWa", "testSetUnionWb", weight = listOf(0.5, 2.0)) shouldBe listOf("y", "x")
    }

    @Test
    suspend fun `test ZINTERSTORE command with fractional weights`() {
        seedWeightedSets("testSetInterStoreW")

        client.zInterStore(
            "testSetInterStoreWd",
            key = arrayOf("testSetInterStoreWa", "testSetInterStoreWb"),
            weight = listOf(0.5, 2.0),
        ) shouldBe 2L
        client.zScore("testSetInterStoreWd", "x") shouldBe 6.5
    }

    @Test
    suspend fun `test ZUNIONSTORE command with fractional weights`() {
        seedWeightedSets("testSetUnionStoreW")

        client.zUnionStore(
            "testSetUnionStoreWd",
            key = arrayOf("testSetUnionStoreWa", "testSetUnionStoreWb"),
            weight = listOf(0.5, 2.0),
        ) shouldBe 2L
        client.zScore("testSetUnionStoreWd", "y") shouldBe 3.5
    }

    @Test
    suspend fun `test weighted sorted set codecs write fractional weights`() {
        val weights = listOf(0.5, 2.0)

        ZInterCommandCodec
            .encode(defaultCharset, "a", "b", weight = weights, aggregate = null, withScores = null)
            .data
            .readString() shouldContain "WEIGHTS\r\n\$3\r\n0.5\r\n\$3\r\n2.0\r\n"
        ZUnionCommandCodec
            .encode(defaultCharset, "a", "b", weight = weights, aggregate = null, withScores = null)
            .data
            .readString() shouldContain "WEIGHTS\r\n\$3\r\n0.5\r\n\$3\r\n2.0\r\n"
        ZInterStoreCommandCodec
            .encode(defaultCharset, "d", "a", "b", weight = weights, aggregate = null)
            .data
            .readString() shouldContain "WEIGHTS\r\n\$3\r\n0.5\r\n\$3\r\n2.0\r\n"
        ZUnionStoreCommandCodec
            .encode(defaultCharset, "d", "a", "b", weight = weights, aggregate = null)
            .data
            .readString() shouldContain "WEIGHTS\r\n\$3\r\n0.5\r\n\$3\r\n2.0\r\n"
    }

    @Test
    suspend fun `test ZCOUNT command with exclusive and infinite bounds`() {
        client.zAdd("testSetCountBounds", *(1..5).map { ZMember("m$it", it.toDouble()) }.toTypedArray())

        client.zCount("testSetCountBounds", "(1", "5") shouldBe 4L
        client.zCount("testSetCountBounds", "(1", "(5") shouldBe 3L
        client.zCount("testSetCountBounds", "-inf", "+inf") shouldBe 5L
    }

    @Test
    suspend fun `test ZCOUNT codec writes bounds verbatim`() {
        ZCountCommandCodec.encode(defaultCharset, "k", "(1", "+inf").data.readString() shouldBe
            "*4\r\n\$6\r\nZCOUNT\r\n\$1\r\nk\r\n\$2\r\n(1\r\n\$4\r\n+inf\r\n"
    }
}
