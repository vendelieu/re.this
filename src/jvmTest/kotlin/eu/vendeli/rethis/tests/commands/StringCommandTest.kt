package eu.vendeli.rethis.tests.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.*
import eu.vendeli.rethis.types.common.LcsResult
import eu.vendeli.rethis.types.options.LcsMode
import eu.vendeli.rethis.types.options.MinMatchLen
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

class StringCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test MGET command`() {
        client.set("testKey1", "testValue1")
        client.set("testKey2", "testValue2")
        client.mGet("testKey1", "testKey2") shouldBe listOf("testValue1", "testValue2")
    }

    @Test
    suspend fun `test MSET command`() {
        client.mSet("testKey3" to "testValue3", "testKey4" to "testValue4") shouldBe true
    }

    @Test
    suspend fun `test MSETNX command`() {
        client.mSetNx("testKey5" to "testValue5", "testKey6" to "testValue6").shouldBeTrue()
    }

    @Test
    suspend fun `test SET command`() {
        client.set("testKey7", "testValue7") shouldBe "OK"
    }

    @Test
    suspend fun `test SETRANGE command`() {
        client.set("testKey8", "testValue8")
        client.setRange("testKey8", 5, "newValue8") shouldBe 14L
    }

    @Test
    suspend fun `test STRLEN command`() {
        val value = "testValue9"
        client.set("testKey9", value)
        client.strlen("testKey9") shouldBe value.length
    }

    @Test
    suspend fun `test LCS command`() {
        client.set("testKey20", "abcdef")
        client.set("testKey21", "zbcdfg")
        client.lcs("testKey20", "testKey21") shouldBe "bcdf"
    }

    @Test
    suspend fun `test LCS command with LEN mode`() {
        client.set("testKey22", "abcdef")
        client.set("testKey23", "zbcdfg")
        client.lcs("testKey22", "testKey23", LcsMode.LEN) shouldBe 4L
    }

    @Test
    suspend fun `test LCS command with MATCHLEN mode`() {
        client.set("testKey24", "abcdef")
        client.set("testKey25", "zbcdfg")

        client.lcs("testKey24", "testKey25", LcsMode.IDX, MinMatchLen(2)).run {
            matches shouldBe listOf(listOf(LcsResult.LcsMatch(1, 3, length = null)))
            totalLength shouldBe 4L
        }
    }

    @Test
    suspend fun `test LCS command with MATCHLEN mode and withMatchLen parameter`() {
        client.set("testKey26", "abcdef")
        client.set("testKey27", "zbcdfg")

        client.lcs("testKey26", "testKey27", LcsMode.IDX, MinMatchLen(2), true).run {
            matches shouldBe listOf(listOf(LcsResult.LcsMatch(1, 3, 3), LcsResult.LcsMatch(1, 3, 3)))
            totalLength shouldBe 4L
        }
    }
}
