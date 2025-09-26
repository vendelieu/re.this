package eu.vendeli.rethis.serde

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.hash.hDel
import eu.vendeli.rethis.command.serde.hGet
import eu.vendeli.rethis.command.serde.hMGet
import eu.vendeli.rethis.command.serde.hSet
import eu.vendeli.rethis.command.serde.hVals
import io.kotest.matchers.shouldBe
import io.ktor.http.quote
import kotlinx.serialization.builtins.PairSerializer
import kotlinx.serialization.builtins.serializer

class HashSerdeCommandsTest : ReThisTestCtx() {
    private val encodedEntity = """{"first":"testValue","second":2}"""
    private val entity = "testValue" to 2

    @Test
    suspend fun `test hGet`() {
        val key = "testKey1"
        val field = "testField"
        val value = entity

        client.hSet(key, field to value)

        val result = client.hGet<String>(key, field)
        result shouldBe encodedEntity

        val serializer = PairSerializer(String.serializer(), Int.serializer())
        val serializedResult = client.hGet(key, field, serializer)
        serializedResult shouldBe entity
    }
    
    

    @Test
    suspend fun `test hVals`() {
        val key = "testKey5"
        val field1 = "testField1"
        val value1 = "testValue1"

        val field2 = "testField2"
        val value2 = entity

        client.hSet(key, field1 to value1)
        client.hSet(key, field2 to value2)

        val result = client.hVals<String>(key)
        result shouldBe listOf(value1.quote(), encodedEntity)

        client.hDel(key, field1)
        val serializedResult = client.hVals(key, TestData.serializer())
        serializedResult.single() shouldBe TestData(entity.first, entity.second)
    }

    @Test
    suspend fun `test hMGet`() {
        val key = "testKey6"
        val field1 = "testField1"
        val value1 = entity

        val field2 = "testField2"
        val value2 = entity

        client.hSet(key, field1 to value1, field2 to value2)

        val result = client.hMGet<String>(key, field1, field2)
        result shouldBe listOf(encodedEntity, encodedEntity)

        val serializer = PairSerializer(String.serializer(), Int.serializer())
        val serializedResult = client.hMGet(key, field = listOf(field1, field2).toTypedArray(), serializer)
        serializedResult shouldBe listOf(entity, entity)
    }
}
