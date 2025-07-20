//package eu.vendeli.rethis.serde
//
//import eu.vendeli.rethis.ReThisTestCtx
//import eu.vendeli.rethis.commands.*
//import eu.vendeli.rethis.types.options.GetExOption
//import eu.vendeli.rethis.utils.__jsonModule
//import io.kotest.matchers.longs.shouldBeInRange
//import io.kotest.matchers.nulls.shouldNotBeNull
//import io.kotest.matchers.shouldBe
//import kotlin.time.Duration.Companion.seconds
//
//class StringSerdeCommandsTest : ReThisTestCtx() {
//    private val entity = TestData("testValue", 2)
//    private val encodedEntity by lazy { client.__jsonModule().encodeToString(entity) }
//
//    @Test
//    suspend fun `test set and get`() {
//        val key = "testKeySet"
//
//        client.set(key, entity)
//
//        // Test retrieving as JSON string
//        val jsonResult = client.get<String>(key)
//        jsonResult shouldBe encodedEntity
//
//        // Test retrieving as deserialized object
//        val objResult = client.get<TestData>(key)
//        objResult shouldBe entity
//    }
//
//    @Test
//    suspend fun `test mSet and mGet`() {
//        val key1 = "testKeyM1"
//        val key2 = "testKeyM2"
//        val value2 = TestData(key2, 8)
//
//        client.mSet(key1 to entity, key2 to value2)
//
//        // Test multi-get as JSON strings
//        val jsonResults = client.mGet<String>(key1, key2)
//        jsonResults shouldBe listOf(encodedEntity, client.__jsonModule().encodeToString(value2))
//
//        // Test multi-get as objects
//        val objResults = client.mGet<TestData>(key1, key2)
//        objResults shouldBe listOf(entity, value2)
//    }
//
//    @Test
//    suspend fun `test getDel`() {
//        val key = "testKeyGetDel"
//
//        client.set(key, entity)
//
//        // Retrieve and delete
//        val result = client.getDel<TestData>(key)
//        result shouldBe entity
//
//        // Verify key is deleted
//        client.get<String>(key) shouldBe null
//    }
//
//    @Test
//    suspend fun `test getEx`() {
//        val key = "testKeyGetEx"
//        val ttlSeconds = 10
//
//        client.set(key, entity)
//
//        // Retrieve with EX option
//        val result = client.getEx<TestData>(key, GetExOption.EX(ttlSeconds.seconds))
//        result shouldBe entity
//
//        // Check remaining TTL is approximately set
//        val ttl = client.ttl(key).shouldNotBeNull()
//
//        ttl shouldBeInRange (ttlSeconds - 2L)..ttlSeconds
//    }
//
//    @Test
//    suspend fun `test lcs`() {
//        val key1 = "testKeyLcs1"
//        val key2 = "testKeyLcs2"
//        val value1 = "ABCD"
//        val value2 = "ACDF"
//
//        client.set(key1, value1)
//        client.set(key2, value2)
//
//        // Test Longest Common Substring
//        val lcsResult = client.lcs<String>(key1, key2)
//        lcsResult shouldBe "ACD"
//    }
//}
