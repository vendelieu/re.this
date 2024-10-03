package eu.vendeli.rethis.tests.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.*
import eu.vendeli.rethis.types.options.LPosOption
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay

class ListCommandTest2 : ReThisTestCtx() {
    @BeforeEach
    suspend fun delayful() {
        delay(100)
    }

    @Test
    suspend fun `test LPOS command with count`() {
        client.lPush("testKey16", "testValue16")
        client.lPush("testKey16", "testValue16")
        delay(200)
        client.lPos("testKey16", "testValue16", LPosOption.Count(2)) shouldBe listOf(0L, 1L)
    }

    @Test
    suspend fun `test LPUSH command`() {
        client.lPush("testKey17", "testValue17") shouldBe 1L
    }

    @Test
    suspend fun `test LPUSHX command`() {
        client.lPush("testKey18", "testValue81")
        client.lPushX("testKey18", "testValue18") shouldBe 2L
    }

    @Test
    suspend fun `test LREM command`() {
        client.lPush("testKey19", "testValue19")
        client.lRem("testKey19", 1, "testValue19") shouldBe 1L
    }

    @Test
    suspend fun `test LSET command`() {
        client.lPush("testKey20", "testValue20")
        client.lSet("testKey20", 0, "newValue20") shouldBe "OK"
    }

    @Test
    suspend fun `test LTRIM command`() {
        client.lPush("testKey21", "testValue21")
        client.lTrim("testKey21", 0, 0) shouldBe "OK"
    }

    @Test
    suspend fun `test RPOP command`() {
        client.rPush("testKey22", "testValue22")
        client.rPop("testKey22") shouldBe "testValue22"
    }

    @Test
    suspend fun `test RPOP command with count`() {
        client.rPush("testKey23", "testValue23")
        client.rPush("testKey23", "testValue24")
        client.rPop("testKey23", 2) shouldBe listOf("testValue24", "testValue23")
    }
}