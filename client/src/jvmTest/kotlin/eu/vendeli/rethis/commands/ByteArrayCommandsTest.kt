package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.hash.hGetAllBA
import eu.vendeli.rethis.command.hash.hGetBA
import eu.vendeli.rethis.command.hash.hRandFieldBA
import eu.vendeli.rethis.command.hash.hSet
import eu.vendeli.rethis.command.list.*
import eu.vendeli.rethis.command.set.sAdd
import eu.vendeli.rethis.command.set.sPopBA
import eu.vendeli.rethis.command.set.sRandMemberBA
import eu.vendeli.rethis.command.sortedset.zAdd
import eu.vendeli.rethis.command.sortedset.zAddBA
import eu.vendeli.rethis.command.sortedset.zRandMemberBA
import eu.vendeli.rethis.command.string.*
import eu.vendeli.rethis.shared.request.common.FieldValue
import eu.vendeli.rethis.shared.response.common.MoveDirection
import eu.vendeli.rethis.shared.response.stream.ZMember
import eu.vendeli.rethis.shared.response.stream.ZMemberBA
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class ByteArrayCommandsTest : ReThisTestCtx() {
    private fun String.toBA(): ByteArray = this.encodeToByteArray()
    private infix fun ByteArray?.shouldDecodeToString(expected: String) {
        this.shouldNotBeNull().decodeToString() shouldBe expected
    }

    // ==================== String Commands ====================

    @Test
    suspend fun `test GET BA command`() {
        client.set("baTestKey1", "testValue1")
        client.getBA("baTestKey1") shouldDecodeToString "testValue1"
    }

    @Test
    suspend fun `test SET BA command`() {
        val value = "testValue2".toBA()
        client.setBA("baTestKey2", value) shouldDecodeToString "OK"
        client.getBA("baTestKey2") shouldDecodeToString "testValue2"
    }

    @Test
    suspend fun `test GETDEL BA command`() {
        client.set("baTestKey3", "testValue3")
        client.getDelBA("baTestKey3") shouldDecodeToString "testValue3"
        client.get("baTestKey3") shouldBe null
    }

    @Test
    suspend fun `test GETEX BA command`() {
        client.set("baTestKey4", "testValue4")
        client.getExBA("baTestKey4") shouldDecodeToString "testValue4"
    }

    @Test
    suspend fun `test GETRANGE BA command`() {
        client.set("baTestKey5", "testValue5")
        client.getRangeBA("baTestKey5", 0L, 4L).decodeToString() shouldBe "testV"
    }

    // ==================== Hash Commands ====================

    @Test
    suspend fun `test HGET BA command`() {
        client.hSet("baHashKey1", FieldValue("field1", "value1"))
        client.hGetBA("baHashKey1", "field1") shouldDecodeToString "value1"
    }

    @Test
    suspend fun `test HGETALL BA command`() {
        client.hSet("baHashKey2", FieldValue("field2", "value2"))
        val result = client.hGetAllBA("baHashKey2")
        result.shouldNotBeNull()
        result["field2"] shouldDecodeToString "value2"
    }

    @Test
    suspend fun `test HRANDFIELD BA command`() {
        client.hSet("baHashKey3", FieldValue("field3", "value3"))
        client.hRandFieldBA("baHashKey3") shouldDecodeToString "field3"
    }

    // ==================== List Commands ====================

    @Test
    suspend fun `test LPOP BA command`() {
        client.lPush("baListKey1", "listValue1")
        client.lPopBA("baListKey1") shouldDecodeToString "listValue1"
    }

    @Test
    suspend fun `test RPUSH BA command`() {
        val value = "listValue2".toBA()
        client.rPushBA("baListKey2", value) shouldBe 1L
        client.lPop("baListKey2") shouldBe "listValue2"
    }

    @Test
    suspend fun `test LMOVE BA command`() {
        client.lPush("baListKey3", "listValue3")
        client
            .lMoveBA("baListKey3", "baListKey4", MoveDirection.LEFT, MoveDirection.LEFT)
            .decodeToString() shouldBe "listValue3"
    }

    @Test
    suspend fun `test BLMOVE BA command`() {
        client.lPush("baListKey5", "listValue5")
        client.blMoveBA(
            "baListKey5",
            "baListKey6",
            MoveDirection.LEFT,
            MoveDirection.LEFT,
            1.0,
        ) shouldDecodeToString "listValue5"
    }

    // ==================== Set Commands ====================

    @Test
    suspend fun `test SPOP BA command`() {
        client.sAdd("baSetKey1", "setValue1")
        client.sPopBA("baSetKey1") shouldDecodeToString "setValue1"
    }

    @Test
    suspend fun `test SRANDMEMBER BA command`() {
        client.sAdd("baSetKey2", "setValue2")
        client.sRandMemberBA("baSetKey2").decodeToString() shouldBe "setValue2"
    }

    // ==================== Sorted Set Commands ====================

    @Test
    suspend fun `test ZADD BA command`() {
        val member = "zsetValue1".toBA()
        client.zAddBA("baZSetKey1", ZMemberBA(member, 1.0)) shouldBe 1L
        client.zRandMemberBA("baZSetKey1") shouldDecodeToString "zsetValue1"
    }

    @Test
    suspend fun `test ZRANDMEMBER BA command`() {
        client.zAdd("baZSetKey2", ZMember("zsetValue2", 1.0))
        client.zRandMemberBA("baZSetKey2").decodeToString() shouldBe "zsetValue2"
    }
}
