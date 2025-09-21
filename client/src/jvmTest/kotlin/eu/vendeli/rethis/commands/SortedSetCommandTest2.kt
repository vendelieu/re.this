package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.sortedset.*
import eu.vendeli.rethis.shared.response.stream.ZMember
import eu.vendeli.rethis.shared.response.stream.ZPopResult
import io.kotest.matchers.shouldBe

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

        client.zCount("testSet9", 0.0, 2.0) shouldBe 1L
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
        client.zIncrBy("testSet15", "testValue15", 1) shouldBe 2.0
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
}
