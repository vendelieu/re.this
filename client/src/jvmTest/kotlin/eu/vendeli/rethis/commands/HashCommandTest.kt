package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.api.spec.common.request.common.FieldValue
import eu.vendeli.rethis.api.spec.common.response.common.ScanResult
import eu.vendeli.rethis.command.hash.*
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class HashCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test HDEL command`() {
        client.hSet("testKey1", FieldValue("testField1", "testValue1"))
        client.hDel("testKey1", "testField1") shouldBe 1L
    }

    @Test
    suspend fun `test HMSET command`() {
        client.hMSet("testKey13", FieldValue("testField13", "testValue13")) shouldBe true
    }

    @Test
    suspend fun `test HEXPIREAT command`() {
        client.hSet("testKey4", FieldValue("testField4", "testValue4"))
        client.hExpireAt("testKey4", timestamp.plus(10.seconds), "testField4") shouldBe listOf(1L)
    }

    @Test
    suspend fun `test HPEXPIRETIME command`() {
        client.hSet("testKey17", FieldValue("testField17", "testValue17"))
        client.hExpireTime("testKey17", "testField17") shouldBe listOf(-1L)
    }

    @Test
    suspend fun `test HPERSIST command`() {
        client.hSet("testKey14", FieldValue("testField14", "testValue14"))
        client.hPersist("testKey14", "testField14") shouldBe listOf(-1L)
    }

    @Test
    suspend fun `test HPEXPIRE command`() {
        client.hSet("testKey15", FieldValue("testField15", "testValue15"))
        client.hExpire("testKey15", 10000.milliseconds, "testField15") shouldBe listOf(1L)
    }

    @Test
    suspend fun `test HPEXPIREAT command`() {
        client.hSet("testKey16", FieldValue("testField16", "testValue16"))

        client.hExpireAt("testKey16", timestamp.plus(10.seconds), "testField16") shouldBe listOf(1L)
    }

    @Test
    suspend fun `test HINCRBYFLOAT command`() {
        client.hSet("testKey9", FieldValue("testField9", "10.5"))
        client.hIncrByFloat("testKey9", "testField9", 5.5) shouldBe 16.0
    }

    @Test
    suspend fun `test HPTTL command`() {
        client.hSet("testKey18", FieldValue("testField18", "testValue18"))
        client.hPttl("testKey18", "testField18") shouldBe listOf(-1L)
    }

    @Test
    suspend fun `test HSCAN command`() {
        client.hSet("testKey20", FieldValue("testField20", "testValue20"))
        client.hScan("testKey20", 0) shouldBe ScanResult(cursor = "0", keys = listOf("testField20" to "testValue20"))
    }

    @Test
    suspend fun `test HSET command`() {
        client.hSet("testKey21", FieldValue("testField21", "testValue21")) shouldBe 1L
    }

    @Test
    suspend fun `test HSETNX command`() {
        client.hSetNx("testKey22", "testField22", "testValue22") shouldBe 1L
    }

    @Test
    suspend fun `test HSTRLEN command`() {
        val value = "testValue23"
        client.hSet("testKey23", FieldValue("testField23", value))

        client.hStrlen("testKey23", "testField23") shouldBe value.length
    }

    @Test
    suspend fun `test HTTL command`() {
        client.hSet("testKey24", FieldValue("testField24", "testValue24"))
        client.hTtl("testKey24", "testField24") shouldBe listOf(-1L)
    }

    @Test
    suspend fun `test HVALS command`() {
        client.hSet("testKey25", FieldValue("testField25", "testValue25"))
        client.hVals("testKey25") shouldBe listOf("testValue25")
    }
}
