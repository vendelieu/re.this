package eu.vendeli.rethis.serde

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.hash.hMSet
import eu.vendeli.rethis.command.hash.hSet
import eu.vendeli.rethis.command.serde.hGet
import eu.vendeli.rethis.command.serde.hMGet
import eu.vendeli.rethis.command.serde.hVals
import eu.vendeli.rethis.shared.request.common.FieldValue
import io.kotest.matchers.shouldBe

class HashSerdeCommandsTest : ReThisTestCtx() {
    private val encodedEntity = """{"first":"testValue","second":2}"""

    @Test
    suspend fun `test hGet`() {
        val key = "testKey1"
        val field = "testField"
        val value = "testValue"

        client.hSet(key, FieldValue(field, value))

        val result = client.hGet<String>(key, field)
        result shouldBe encodedEntity
    }

    @Test
    suspend fun `test hMGet`() {
        val key = "testKey2"
        val field1 = "testField1"
        val value1 = "testValue1"

        val field2 = "testField2"
        val value2 = "testValue2"

        client.hSet(key, FieldValue(field1, value1))
        client.hSet(key, FieldValue(field2, value2))

        val result = client.hMGet<String>(key, field1, field2)
        result shouldBe listOf(value1, encodedEntity)
    }

    @Test
    suspend fun `test hMSet`() {
        val key = "testKey3"
        val field1 = "testField1"

        val field2 = "testField2"
        val value2 = "testValue2"

        val result = client.hMSet(key, FieldValue(field2, value2))
        result shouldBe true

        val values = client.hMGet<String>(key, field1, field2)
        values shouldBe listOf(null, encodedEntity)
    }

    @Test
    suspend fun `test hVals`() {
        val key = "testKey5"
        val field1 = "testField1"
        val value1 = "testValue1"

        val field2 = "testField2"
        val value2 = "testValue2"

        client.hSet(key, FieldValue(field1, value1))
        client.hSet(key, FieldValue(field2, value2))

        val result = client.hVals<String>(key)
        result shouldBe listOf(value1, encodedEntity)
    }
}
