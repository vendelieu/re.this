package eu.vendeli.rethis.tests.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.*
import eu.vendeli.rethis.types.common.ZMember
import eu.vendeli.rethis.types.common.ZPopResult
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class SortedSetCommandTest2 : ReThisTestCtx() {
    @Test
    fun `test BZPOPMIN command`(): Unit = runBlocking {
        client.zAdd("testSet5", ZMember("testValue5", 1.0))

        client.bzPopMin(1.0, "testSet5", "testSet6") shouldBe ZPopResult("testSet5", "testValue5", 1.0)
    }

    @Test
    fun `test ZADD command`(): Unit = runBlocking {
        client.zAdd("testSet7", ZMember("testValue7", 1.0)) shouldBe 1L
    }

    @Test
    fun `test ZREVRANK command`(): Unit = runBlocking {
        client.zAdd("testSet37", ZMember("testValue37", 1.0))

        client.zRevrank("testSet37", "testValue37") shouldBe 0L
    }

    @Test
    fun `test ZCARD command`(): Unit = runBlocking {
        client.zAdd("testSet8", ZMember("testValue8", 1.0))

        client.zCard("testSet8") shouldBe 1L
    }

    @Test
    fun `test ZCOUNT command`(): Unit = runBlocking {
        client.zAdd("testSet9", ZMember("testValue9", 1.0))

        client.zCount("testSet9", 0.0, 2.0) shouldBe 1L
    }

    @Test
    fun `test ZDIFF command`(): Unit = runBlocking {
        client.zAdd("testSet10", ZMember("testValue10", 1.0))
        client.zAdd("testSet11", ZMember("testValue11", 2.0))

        client.zDiff("testSet10", "testSet11") shouldBe listOf("testValue10")
    }

    @Test
    fun `test ZDIFFSTORE command`(): Unit = runBlocking {
        client.zAdd("testSet12", ZMember("testValue12", 1.0))
        client.zAdd("testSet13", ZMember("testValue13", 2.0))

        client.zDiffStore("testSet14", "testSet12", "testSet13") shouldBe 1L
    }

    @Test
    fun `test ZINCRBY command`(): Unit = runBlocking {
        client.zAdd("testSet15", ZMember("testValue15", 1.0))
        client.zIncrby("testSet15", "testValue15", 1.0) shouldBe 2.0
    }

    @Test
    fun `test ZINTER command`(): Unit = runBlocking {
        client.zAdd("testSet16", ZMember("testValue16", 1.0))
        client.zAdd("testSet17", ZMember("testValue16", 2.0))

        client.zInter("testSet16", "testSet17") shouldBe listOf("testValue16")
    }

    @Test
    fun `test ZINTERCARD command`(): Unit = runBlocking {
        client.zAdd("testSet18", ZMember("testValue18", 1.0))
        client.zAdd("testSet19", ZMember("testValue18", 2.0))

        client.zInterCard("testSet18", "testSet19") shouldBe 1L
    }

    @Test
    fun `test ZINTERSTORE command`(): Unit = runBlocking {
        client.zAdd("testSet20", ZMember("testValue20", 1.0))
        client.zAdd("testSet21", ZMember("testValue20", 2.0))

        client.zInterStore("testSet22", "testSet20", "testSet21") shouldBe 1L
    }
}
