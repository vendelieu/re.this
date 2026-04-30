package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.cast
import eu.vendeli.rethis.command.json.*
import eu.vendeli.rethis.shared.request.json.JsonEntry
import eu.vendeli.rethis.shared.types.*
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.io.readString

class JsonCommandTest2 : ReThisTestCtx() {
    @Test
    suspend fun `test JSON_MGET command`() {
        client.jsonSet("testKey13", "[1, 2, 3]", ".")
        client.jsonMGet(key = arrayOf("testKey13"), path = ".") shouldBe listOf("[1,2,3]")
    }

    @Test
    suspend fun `test JSON_MSET command`() {
        client.jsonSet("testKey14", "[1, 2, 3]", ".")
        client.jsonMSet(JsonEntry("testKey14", ".", "[4, 5, 6]")) shouldBe true
    }

    @Test
    suspend fun `test JSON_NUMINCRBY command`() {
        client.jsonSet("testKey15", "{\"a\":\"b\",\"b\":[{\"a\":2}, {\"a\":5}, {\"a\":\"c\"}]}", ".")
        val result = client.jsonNumIncrBy("testKey15", 2.0, "..a")
        if (result is BulkString) {
            result shouldBe BulkString(value = "7.0") // 7.4 behavior
            return
        }

        result.cast<RArray>().value.run {
            first() shouldBe RType.Null
            get(1) shouldBe F64(value = 4.0)
            get(2) shouldBe F64(value = 7.0)
            last() shouldBe RType.Null
        }
    }

    @Test
    suspend fun `test JSON_NUMMULTBY command`() {
        client.jsonSet(
            "testKey16",
            "{\"a\":\"b\",\"b\":[{\"a\":2}, {\"a\":5}, {\"a\":\"c\"}]}",
            ".",
        )
        val result = client.jsonNumMultBy("testKey16", 2.0, "..a")
        if (result is BulkString) {
            result shouldBe BulkString(value = "10.0") // 7.4 behavior
            return
        }
        result.cast<RArray>().value.run {
            first() shouldBe RType.Null
            get(1) shouldBe F64(value = 4.0)
            get(2) shouldBe F64(value = 10.0)
            last() shouldBe RType.Null
        }
    }

    @Test
    suspend fun `test JSON_OBJKEYS command`() {
        client.jsonSet("testKey17", "{\"a\": 1, \"b\": 2}", ".")
        client.jsonObjKeys("testKey17", ".") shouldBe listOf("a", "b")
    }

    @Test
    suspend fun `test JSON_OBJLEN command`() {
        client.jsonSet("testKey18", "{\"a\":[3], \"nested\": {\"a\": {\"b\":2, \"c\": 1}}}", ".")
        client.jsonObjLen("testKey18", "$..a").shouldBeTypeOf<RArray>().value shouldBe listOf(
            RType.Null,
            Int64(2),
        )
    }

    @Test
    suspend fun `test JSON_RESP command`() {
        client.jsonSet("testKey19", "[1, 2, 3]", ".")
        client.jsonResp("testKey19") shouldBe listOf(PlainString("["), Int64(1), Int64(2), Int64(3))
    }

    @Test
    suspend fun `test JSON_SET command`() {
        client.jsonSet("testKey20", "[1, 2, 3]", ".") shouldBe "OK"
    }

    @Test
    suspend fun `test JSON_STRAPPEND command`() {
        client.jsonSet(
            "testKey21",
            "{\"a\":\"foo\", \"nested\": {\"a\": \"hello\"}, \"nested2\": {\"a\": 31}}",
            ".",
        ) shouldBe "OK"

        client.jsonStrAppend("testKey21", "\"baz\"", "..a") shouldBe 8L
    }

    @Test
    suspend fun `test JSON_STRLEN command`() {
        client.jsonSet("testKey22", "\"hello\"", ".")

        client.jsonStrLen("testKey22", ".") shouldBe 5L
    }

    @Test
    suspend fun `test JSON_TOGGLE command`() {
        client.jsonSet("testKey23", "{\"bool\": true}") shouldBe "OK"
        client.jsonToggle("testKey23", "$.bool").shouldBeTypeOf<RArray>().value shouldBe listOf(Int64(0))
    }

    @Test
    suspend fun `test JSON_TYPE command`() {
        client.jsonSet("testKey24", "[1, 2, 3]", ".")
        client.jsonType("testKey24", ".").let {
            if (it is BulkString) {
                it.value.readString()
            } else {
                it
                    .cast<RArray>()
                    .value
                    .first()
                    .cast<BulkString>()
                    .value
                    .readString()
            }
        } shouldBe "array"
    }
}
