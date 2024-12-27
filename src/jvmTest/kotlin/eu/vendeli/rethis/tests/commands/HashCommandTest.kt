package eu.vendeli.rethis.tests.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.*
import eu.vendeli.rethis.types.common.ScanResult
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class HashCommandTest : ReThisTestCtx() {
    @Test
    fun `test HDEL command`(): Unit = runBlocking {
        client.hSet("testKey1", "testField1" to "testValue1")
        client.hDel("testKey1", "testField1") shouldBe 1L
    }

    @Test
    fun `test HMSET command`(): Unit = runBlocking {
        client.hMSet("testKey13", "testField13" to "testValue13") shouldBe "OK"
    }

    @Test
    fun `test HEXPIREAT command`(): Unit = runBlocking {
        client.hSet("testKey4", "testField4" to "testValue4")
        client.hExpireAt("testKey4", timestamp.plus(10.seconds), "testField4") shouldBe listOf(1L)
    }

    @Test
    fun `test HPEXPIRETIME command`(): Unit = runBlocking {
        client.hSet("testKey17", "testField17" to "testValue17")
        client.hPExpireTime("testKey17", "testField17") shouldBe listOf(-1L)
    }

    @Test
    fun `test HPERSIST command`(): Unit = runBlocking {
        client.hSet("testKey14", "testField14" to "testValue14")
        client.hPersist("testKey14", "testField14") shouldBe listOf(-1L)
    }

    @Test
    fun `test HPEXPIRE command`(): Unit = runBlocking {
        client.hSet("testKey15", "testField15" to "testValue15")
        client.hPExpire("testKey15", 10000.milliseconds, "testField15") shouldBe listOf(1L)
    }

    @Test
    fun `test HPEXPIREAT command`(): Unit = runBlocking {
        client.hSet("testKey16", "testField16" to "testValue16")

        client.hPExpireAt("testKey16", timestamp.plus(10.seconds), "testField16") shouldBe listOf(1L)
    }

    @Test
    fun `test HINCRBYFLOAT command`(): Unit = runBlocking {
        client.hSet("testKey9", "testField9" to "10.5")
        client.hIncrByFloat("testKey9", "testField9", 5.5) shouldBe 16.0
    }

    @Test
    fun `test HPTTL command`(): Unit = runBlocking {
        client.hSet("testKey18", "testField18" to "testValue18")
        client.hPTTL("testKey18", "testField18") shouldBe listOf(-1L)
    }

    @Test
    fun `test HSCAN command`(): Unit = runBlocking {
        client.hSet("testKey20", "testField20" to "testValue20")
        client.hScan("testKey20", 0) shouldBe ScanResult(cursor = "0", keys = listOf("testField20" to "testValue20"))
    }

    @Test
    fun `test HSET command`(): Unit = runBlocking {
        client.hSet("testKey21", "testField21" to "testValue21") shouldBe 1L
    }

    @Test
    fun `test HSETNX command`(): Unit = runBlocking {
        client.hSetNx("testKey22", "testField22" to "testValue22") shouldBe 1L
    }

    @Test
    fun `test HSTRLEN command`(): Unit = runBlocking {
        val value = "testValue23"
        client.hSet("testKey23", "testField23" to value)

        client.hStrlen("testKey23", "testField23") shouldBe value.length
    }

    @Test
    fun `test HTTL command`(): Unit = runBlocking {
        client.hSet("testKey24", "testField24" to "testValue24")
        client.hTTL("testKey24", "testField24") shouldBe listOf(-1L)
    }

    @Test
    fun `test HVALS command`(): Unit = runBlocking {
        client.hSet("testKey25", "testField25" to "testValue25")
        client.hVals("testKey25") shouldBe listOf("testValue25")
    }
}
