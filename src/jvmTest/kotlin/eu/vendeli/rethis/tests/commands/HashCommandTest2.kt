package eu.vendeli.rethis.tests.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.*
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class HashCommandTest2 : ReThisTestCtx() {
    @Test
    fun `test HEXISTS command`(): Unit = runTest {
        client.hSet("testKey2", "testField2" to "testValue2")
        client.hExists("testKey2", "testField2") shouldBe true
    }

    @Test
    fun `test HEXPIRE command`(): Unit = runTest {
        client.hSet("testKey3", "testField3" to "testValue3")
        client.hExpire("testKey3", 10.seconds, "testField3") shouldBe listOf(1L)
    }

    @Test
    fun `test HRANDFIELD command`(): Unit = runTest {
        client.hSet("testKey19", "testField19" to "testValue19")

        client.hRandField("testKey19") shouldBe "testField19"
    }

    @Test
    fun `test HEXPIRETIME command`(): Unit = runTest {
        client.hSet("testKey5", "testField5" to "testValue5")
        client.hExpireTime("testKey5", "testField5") shouldBe listOf(-1L)
    }

    @Test
    fun `test HGET command`(): Unit = runTest {
        client.hSet("testKey6", "testField6" to "testValue6")
        client.hGet("testKey6", "testField6") shouldBe "testValue6"
    }

    @Test
    fun `test HGETALL command`(): Unit = runTest {
        client.hSet("testKey7", "testField7" to "testValue7")
        client.hGetAll("testKey7").shouldNotBeNull().toList() shouldBe listOf("testField7" to "testValue7")
    }

    @Test
    fun `test HINCRBY command`(): Unit = runTest {
        client.hSet("testKey8", "testField8" to "10")
        client.hIncrBy("testKey8", "testField8", 5) shouldBe 15L
    }

    @Test
    fun `test HKEYS command`(): Unit = runTest {
        client.hSet("testKey10", "testField10" to "testValue10")
        client.hKeys("testKey10") shouldBe listOf("testField10")
    }

    @Test
    fun `test HLEN command`(): Unit = runTest {
        client.hSet("testKey11", "testField11" to "testValue11")
        client.hLen("testKey11") shouldBe 1L
    }

    @Test
    fun `test HMGET command`(): Unit = runTest {
        client.hSet("testKey12", "testField12" to "testValue12")
        client.hMGet("testKey12", "testField12") shouldBe listOf("testValue12")
    }
}
