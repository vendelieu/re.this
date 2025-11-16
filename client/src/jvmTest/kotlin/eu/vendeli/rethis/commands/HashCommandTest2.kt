package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.hash.*
import eu.vendeli.rethis.shared.request.common.FieldValue
import eu.vendeli.rethis.shared.utils.unwrap
import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.seconds

class HashCommandTest2 : ReThisTestCtx() {
    @Test
    suspend fun `test HEXISTS command`() {
        client.hSet("testKey2", FieldValue("testField2", "testValue2"))
        client.hExists("testKey2", "testField2") shouldBe true
    }

    @Test
    suspend fun `test HEXPIRE command`() {
        client.hSet("testKey3", FieldValue("testField3", "testValue3"))
        client.hExpire("testKey3", 10.seconds, "testField3") shouldBe listOf(1L)
    }

    @Test
    suspend fun `test HRANDFIELD command`() {
        client.hSet("testKey19", FieldValue("testField19", "testValue19"))

        client.hRandField("testKey19") shouldBe "testField19"
    }

    @Test
    suspend fun `test HRANDFIELD command with count and withValues option`() {
        client.hSet(
            "testKey20",
            FieldValue("field1", "value1"),
            FieldValue("field2", "value2"),
            FieldValue("field3", "value3"),
        )

        val fields = client.hRandFieldCount("testKey20", 2, true).map {
            it.unwrap<String>()!!
        }
        fields.size shouldBe 4

        val allFields = listOf("field1", "field2", "field3")
        val allValues = listOf("value1", "value2", "value3")

        fields.first() shouldBeIn allFields
        fields.get(1) shouldBeIn allValues
        fields.get(2) shouldBeIn allFields
        fields.last() shouldBeIn allValues
    }

    @Test
    suspend fun `test HEXPIRETIME command`() {
        client.hSet("testKey5", FieldValue("testField5", "testValue5"))
        client.hExpireTime("testKey5", "testField5") shouldBe listOf(-1L)
    }

    @Test
    suspend fun `test HGET command`() {
        client.hSet("testKey6", FieldValue("testField6", "testValue6"))
        client.hGet("testKey6", "testField6") shouldBe "testValue6"
    }

    @Test
    suspend fun `test HGETALL command`() {
        client.hSet("testKey7", FieldValue("testField7", "testValue7"))
        client.hGetAll("testKey7").shouldNotBeNull().toList() shouldBe listOf("testField7" to "testValue7")
    }

    @Test
    suspend fun `test HINCRBY command`() {
        client.hSet("testKey8", FieldValue("testField8", "10"))
        client.hIncrBy("testKey8", "testField8", 5) shouldBe 15L
    }

    @Test
    suspend fun `test HKEYS command`() {
        client.hSet("testKey10", FieldValue("testField10", "testValue10"))
        client.hKeys("testKey10") shouldBe listOf("testField10")
    }

    @Test
    suspend fun `test HLEN command`() {
        client.hSet("testKey11", FieldValue("testField11", "testValue11"))
        client.hLen("testKey11") shouldBe 1L
    }

    @Test
    suspend fun `test HMGET command`() {
        client.hSet("testKey12", FieldValue("testField12", "testValue12"))
        client.hMGet("testKey12", "testField12") shouldBe listOf("testValue12")
    }
}
