package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.set.*
import io.kotest.matchers.shouldBe

class SetCommandTest2 : ReThisTestCtx() {
    @Test
    suspend fun `test SADD command with multiple members`() {
        client.sAdd("testKey2", "testMember2", "testMember3", "testMember4") shouldBe 3L
    }

    @Test
    suspend fun `test SCARD command`() {
        client.sAdd("testKey5", "testMember5")

        client.sCard("testKey5") shouldBe 1L
    }

    @Test
    suspend fun `test SDIFF command`() {
        client.sAdd("testKey6", "testMember6", "testMember7")
        client.sAdd("testKey7", "testMember7")

        client.sDiff("testKey6", "testKey7") shouldBe listOf("testMember6")
    }

    @Test
    suspend fun `test SDIFFSTORE command`() {
        client.sAdd("testKey8", "testMember8")
        client.sAdd("testKey9", "testMember9")

        client.sDiffStore("testKey10", "testKey8", "testKey9") shouldBe 1L
    }

    @Test
    suspend fun `test SINTER command`() {
        client.sAdd("testKey11", "testMember11")
        client.sAdd("testKey11", "testMember12")
        client.sAdd("testKey12", "testMember12")

        client.sInter("testKey11", "testKey12") shouldBe listOf("testMember12")
    }

    @Test
    suspend fun `test SINTERSTORE command`() {
        client.sAdd("testKey13", "testMember13")
        client.sAdd("testKey13", "testMember14")
        client.sAdd("testKey14", "testMember14")

        client.sInterStore("testKey15", "testKey13", "testKey14") shouldBe 1L
    }

    @Test
    suspend fun `test SISMEMBER command`() {
        client.sAdd("testKey16", "testMember16")

        client.sIsMember("testKey16", "testMember16") shouldBe true
    }

    @Test
    suspend fun `test SMEMBERS command`() {
        client.sAdd("testKey17", "testMember17")

        client.sMembers("testKey17") shouldBe listOf("testMember17")
    }

    @Test
    suspend fun `test SMOVE command`() {
        client.sAdd("testKey18", "testMember18")
        client.sMove("testKey18", "testKey19", "testMember18") shouldBe true
    }
}
