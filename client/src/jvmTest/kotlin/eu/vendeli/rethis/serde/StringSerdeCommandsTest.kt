package eu.vendeli.rethis.serde

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.serde.get
import eu.vendeli.rethis.command.serde.mGet
import eu.vendeli.rethis.command.serde.mSet
import eu.vendeli.rethis.command.serde.set
import eu.vendeli.rethis.utils.serdeModule
import io.kotest.matchers.shouldBe

class StringSerdeCommandsTest : ReThisTestCtx() {
    private val entity = TestData("testValue", 2)
    private val encodedEntity by lazy {
        client.serdeModule().serialize(TestData.serializer(), entity)
    }

    @Test
    suspend fun `test set and get`() {
        val key = "testKeySet"

        client.set(key, entity)

        // Test retrieving as JSON string
        val jsonResult = client.get<String>(key)
        jsonResult shouldBe encodedEntity

        // Test retrieving as deserialized object
        val objResult = client.get<TestData>(key)
        objResult shouldBe entity
    }

    @Test
    suspend fun `test mSet and mGet`() {
        val key1 = "testKeyM1"
        val key2 = "testKeyM2"
        val value2 = TestData(key2, 8)

        client.mSet(key1 to entity, key2 to value2)

        // Test multi-get as JSON strings
        val jsonResults = client.mGet<String>(key1, key2)
        jsonResults shouldBe listOf(
            encodedEntity,
            client.serdeModule().serialize(TestData.serializer(), value2),
        )

        // Test multi-get as objects
        val objResults = client.mGet<TestData>(key1, key2)
        objResults shouldBe listOf(entity, value2)
    }
}
