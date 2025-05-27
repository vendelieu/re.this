package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.*
import eu.vendeli.rethis.types.response.ScanResult
import eu.vendeli.rethis.types.options.SScanOption
import io.kotest.matchers.shouldBe

class SetCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test SADD command with single member`() {
        client.sAdd("testKey1", "testMember1") shouldBe 1L
    }

    @Test
    suspend fun `test SPOP command without count`() {
        client.sAdd("testKey20", "testMember20")
        client.sPop("testKey20") shouldBe "testMember20"
    }

    @Test
    suspend fun `test SPOP command with count`() {
        client.sAdd("testKey21", "testMember21")
        client.sAdd("testKey21", "testMember22")
        client.sPop("testKey21", 2) shouldBe listOf("testMember21", "testMember22")
    }

    @Test
    suspend fun `test SRANDMEMBER command without count`() {
        client.sAdd("testKey23", "testMember23")
        client.sRandMember("testKey23") shouldBe "testMember23"
    }

    @Test
    suspend fun `test SRANDMEMBER command with count`() {
        client.sAdd("testKey24", "testMember24")
        client.sAdd("testKey24", "testMember25")
        client.sRandMember("testKey24", 2) shouldBe listOf("testMember24", "testMember25")
    }

    @Test
    suspend fun `test SREM command with single member`() {
        client.sAdd("testKey26", "testMember26")
        client.sRem("testKey26", "testMember26") shouldBe 1L
    }

    @Test
    suspend fun `test SREM command with multiple members`() {
        client.sAdd("testKey27", "testMember27")
        client.sAdd("testKey27", "testMember28")
        client.sAdd("testKey27", "testMember29")

        client.sRem("testKey27", "testMember27", "testMember28", "testMember29") shouldBe 3L
    }

    @Test
    suspend fun `test SSCAN command`() {
        client.sAdd("testKey30", "testMember30")
        client.sScan("testKey30", 0, SScanOption.MATCH("*")) shouldBe ScanResult("0", listOf("testMember30"))
    }

    @Test
    suspend fun `test SUNION command`() {
        client.sAdd("testKey31", "testMember31")
        client.sAdd("testKey32", "testMember32")

        client.sUnion("testKey31", "testKey32") shouldBe listOf("testMember31", "testMember32")
    }

    @Test
    suspend fun `test SUNIONSTORE command`() {
        client.sAdd("testKey33", "testMember33")
        client.sAdd("testKey34", "testMember34")

        client.sUnionStore("testKey35", "testKey33", "testKey34") shouldBe 2L
    }

    @Test
    suspend fun `test SINTERCARD command`() {
        client.sAdd("testSet35", "testValue1", "testValue2")
        client.sAdd("testSet36", "testValue2", "testValue3")

        client.sInterCard("testSet35", "testSet36") shouldBe 1L
    }

    @Test
    suspend fun `test SMISMEMBER command`() {
        client.sAdd("testSet37", "testValue1", "testValue2")

        client.sMisMember("testSet37", "testValue1", "testValue2", "testValue3") shouldBe listOf(true, true, false)
    }
}
