package eu.vendeli.rethis.tests.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.*
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class SetCommandTest2 : ReThisTestCtx() {
    @Test
    fun `test SADD command with multiple members`(): Unit = runBlocking {
        client.sAdd("testKey2", "testMember2", "testMember3", "testMember4") shouldBe 3L
    }

    @Test
    fun `test SCARD command`(): Unit = runBlocking {
        client.sAdd("testKey5", "testMember5")

        client.sCard("testKey5") shouldBe 1L
    }

    @Test
    fun `test SDIFF command`(): Unit = runBlocking {
        client.sAdd("testKey6", "testMember6", "testMember7")
        client.sAdd("testKey7", "testMember7")

        client.sDiff("testKey6", "testKey7") shouldBe listOf("testMember6")
    }

    @Test
    fun `test SDIFFSTORE command`(): Unit = runBlocking {
        client.sAdd("testKey8", "testMember8")
        client.sAdd("testKey9", "testMember9")

        client.sDiffStore("testKey10", "testKey8", "testKey9") shouldBe 1L
    }

    @Test
    fun `test SINTER command`(): Unit = runBlocking {
        client.sAdd("testKey11", "testMember11")
        client.sAdd("testKey11", "testMember12")
        client.sAdd("testKey12", "testMember12")

        client.sInter("testKey11", "testKey12") shouldBe listOf("testMember12")
    }

    @Test
    fun `test SINTERSTORE command`(): Unit = runBlocking {
        client.sAdd("testKey13", "testMember13")
        client.sAdd("testKey13", "testMember14")
        client.sAdd("testKey14", "testMember14")

        client.sInterStore("testKey15", "testKey13", "testKey14") shouldBe 1L
    }

    @Test
    fun `test SISMEMBER command`(): Unit = runBlocking {
        client.sAdd("testKey16", "testMember16")

        client.sIsMember("testKey16", "testMember16") shouldBe true
    }

    @Test
    fun `test SMEMBERS command`(): Unit = runBlocking {
        client.sAdd("testKey17", "testMember17")

        client.sMembers("testKey17") shouldBe listOf("testMember17")
    }

    @Test
    fun `test SMOVE command`(): Unit = runBlocking {
        client.sAdd("testKey18", "testMember18")
        client.sMove("testKey18", "testKey19", "testMember18") shouldBe true
    }
}
