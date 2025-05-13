package eu.vendeli.rethis.tests.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.*
import eu.vendeli.rethis.types.response.ScanResult
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class HashCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test HDEL command`() {
        client.hSet("testKey1", "testField1" to "testValue1")
        client.hDel("testKey1", "testField1") shouldBe 1L
    }

    @Test
    suspend fun `test HMSET command`() {
        client.hMSet("testKey13", "testField13" to "testValue13") shouldBe true
    }

    @Test
    suspend fun `test HEXPIREAT command`() {
        client.hSet("testKey4", "testField4" to "testValue4")
        client.hExpireAt("testKey4", timestamp.plus(10.seconds), "testField4") shouldBe listOf(1L)
    }

    @Test
    suspend fun `test HPEXPIRETIME command`() {
        client.hSet("testKey17", "testField17" to "testValue17")
        client.hPExpireTime("testKey17", "testField17") shouldBe listOf(-1L)
    }

    @Test
    suspend fun `test HPERSIST command`() {
        client.hSet("testKey14", "testField14" to "testValue14")
        client.hPersist("testKey14", "testField14") shouldBe listOf(-1L)
    }

    @Test
    suspend fun `test HPEXPIRE command`() {
        client.hSet("testKey15", "testField15" to "testValue15")
        client.hPExpire("testKey15", 10000.milliseconds, "testField15") shouldBe listOf(1L)
    }

    @Test
    suspend fun `test HPEXPIREAT command`() {
        client.hSet("testKey16", "testField16" to "testValue16")

        client.hPExpireAt("testKey16", timestamp.plus(10.seconds), "testField16") shouldBe listOf(1L)
    }

    @Test
    suspend fun `test HINCRBYFLOAT command`() {
        client.hSet("testKey9", "testField9" to "10.5")
        client.hIncrByFloat("testKey9", "testField9", 5.5) shouldBe 16.0
    }

    @Test
    suspend fun `test HPTTL command`() {
        client.hSet("testKey18", "testField18" to "testValue18")
        client.hPTTL("testKey18", "testField18") shouldBe listOf(-1L)
    }

    @Test
    suspend fun `test HSCAN command`() {
        client.hSet("testKey20", "testField20" to "testValue20")
        client.hScan("testKey20", 0) shouldBe ScanResult(cursor = "0", keys = listOf("testField20" to "testValue20"))
    }

    @Test
    suspend fun `test HSET command`() {
        client.hSet("testKey21", "testField21" to "testValue21") shouldBe 1L
    }

    @Test
    suspend fun `test HSETNX command`() {
        client.hSetNx("testKey22", "testField22" to "testValue22") shouldBe 1L
    }

    @Test
    suspend fun `test HSTRLEN command`() {
        val value = "testValue23"
        client.hSet("testKey23", "testField23" to value)

        client.hStrlen("testKey23", "testField23") shouldBe value.length
    }

    @Test
    suspend fun `test HTTL command`() {
        client.hSet("testKey24", "testField24" to "testValue24")
        client.hTTL("testKey24", "testField24") shouldBe listOf(-1L)
    }

    @Test
    suspend fun `test HVALS command`() {
        client.hSet("testKey25", "testField25" to "testValue25")
        client.hVals("testKey25") shouldBe listOf("testValue25")
    }
}
