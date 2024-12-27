package eu.vendeli.rethis.tests.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.*
import eu.vendeli.rethis.types.options.LPosOption
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class ListCommandTest2 : ReThisTestCtx() {
    @Test
    fun `test LPOS command with count`(): Unit = runBlocking {
        client.lPush("testKey16", "testValue16")
        client.lPush("testKey16", "testValue16")

        client.lPos("testKey16", "testValue16", LPosOption.Count(2)) shouldBe listOf(0L, 1L)
    }

    @Test
    fun `test LPUSH command`(): Unit = runBlocking {
        client.lPush("testKey17", "testValue17") shouldBe 1L
    }

    @Test
    fun `test LPUSHX command`(): Unit = runBlocking {
        client.lPush("testKey18", "testValue81")
        client.lPushX("testKey18", "testValue18") shouldBe 2L
    }

    @Test
    fun `test LREM command`(): Unit = runBlocking {
        client.lPush("testKey19", "testValue19")
        client.lRem("testKey19", 1, "testValue19") shouldBe 1L
    }

    @Test
    fun `test LRANGE command`(): Unit = runBlocking {
        client.lPush("testKey00", "testValue00")
        client.lRange("testKey00", 0, -1) shouldHaveSize 1
    }

    @Test
    fun `test LSET command`(): Unit = runBlocking {
        client.lPush("testKey20", "testValue20")
        client.lSet("testKey20", 0, "newValue20") shouldBe "OK"
    }

    @Test
    fun `test LTRIM command`(): Unit = runBlocking {
        client.lPush("testKey21", "testValue21")
        client.lTrim("testKey21", 0..0) shouldBe "OK"
    }

    @Test
    fun `test RPOP command`(): Unit = runBlocking {
        client.rPush("testKey22", "testValue22")
        client.rPop("testKey22") shouldBe "testValue22"
    }

    @Test
    fun `test RPOP command with count`(): Unit = runBlocking {
        client.rPush("testKey23", "testValue23")
        client.rPush("testKey23", "testValue24")
        client.rPop("testKey23", 2) shouldBe listOf("testValue24", "testValue23")
    }
}
