package eu.vendeli.rethis.tests.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.*
import eu.vendeli.rethis.types.core.Int64
import eu.vendeli.rethis.types.core.PlainString
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class JsonCommandTest2 : ReThisTestCtx(true) {
    @Test
    fun `test JSON_MGET command`(): Unit = runBlocking {
        client.jsonSet("testKey13", ".", "[1, 2, 3]")
        client.jsonMGet("testKey13", ".") shouldBe listOf("[1,2,3]")
    }

    @Test
    fun `test JSON_MSET command`(): Unit = runBlocking {
        client.jsonSet("testKey14", ".", "[1, 2, 3]")
        client.jsonMSet("testKey14", ".", "[4, 5, 6]") shouldBe "OK"
    }

    @Test
    fun `test JSON_NUMINCRBY command`(): Unit = runBlocking {
        client.jsonSet("testKey15", ".", "{\"a\":\"b\",\"b\":[{\"a\":2}, {\"a\":5}, {\"a\":\"c\"}]}")
        client.jsonNumIncrBy("testKey15", "..a", 2) shouldBe listOf(4, 7L)
    }

    @Test
    fun `test JSON_NUMMULTBY command`(): Unit = runBlocking {
        client.jsonSet("testKey16", ".", "{\"a\":\"b\",\"b\":[{\"a\":2}, {\"a\":5}, {\"a\":\"c\"}]}")
        client.jsonNumMultBy("testKey16", "..a", 2) shouldBe listOf(4, 10L)
    }

    @Test
    fun `test JSON_OBJKEYS command`(): Unit = runBlocking {
        client.jsonSet("testKey17", ".", "{\"a\": 1, \"b\": 2}")
        client.jsonObjKeys("testKey17", ".") shouldBe listOf("a", "b")
    }

    @Test
    fun `test JSON_OBJLEN command`(): Unit = runBlocking {
        client.jsonSet("testKey18", ".", "{\"a\":[3], \"nested\": {\"a\": {\"b\":2, \"c\": 1}}}")
        client.jsonObjLen("testKey18", "$..a") shouldBe listOf(2L)
    }

    @Test
    fun `test JSON_RESP command`(): Unit = runBlocking {
        client.jsonSet("testKey19", ".", "[1, 2, 3]")
        client.jsonResp("testKey19") shouldBe listOf(PlainString("["), Int64(1), Int64(2), Int64(3))
    }

    @Test
    fun `test JSON_SET command`(): Unit = runBlocking {
        client.jsonSet("testKey20", ".", "[1, 2, 3]") shouldBe "OK"
    }

    @Test
    fun `test JSON_STRAPPEND command`(): Unit = runBlocking {
        client.jsonSet(
            "testKey21",
            ".",
            "{\"a\":\"foo\", \"nested\": {\"a\": \"hello\"}, \"nested2\": {\"a\": 31}}",
        ) shouldBe "OK"

        client.jsonStrAppend("testKey21", "\"baz\"", "..a") shouldBe 8L
    }

    @Test
    fun `test JSON_STRLEN command`(): Unit = runBlocking {
        client.jsonSet("testKey22", ".", "\"hello\"")

        client.jsonStrLen("testKey22", ".") shouldBe 5L
    }

    @Test
    fun `test JSON_TOGGLE command`(): Unit = runBlocking {
        client.jsonSet("testKey23", "$", "{\"bool\": true}") shouldBe "OK"
        client.jsonToggle("testKey23", "$.bool") shouldBe listOf(0L)
    }

    @Test
    fun `test JSON_TYPE command`(): Unit = runBlocking {
        client.jsonSet("testKey24", ".", "[1,2,3]")
        client.jsonType("testKey24", ".") shouldBe listOf("array")
    }
}
