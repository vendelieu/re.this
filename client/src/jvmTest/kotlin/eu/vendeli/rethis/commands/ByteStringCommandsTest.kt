package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.hash.*
import eu.vendeli.rethis.command.list.*
import eu.vendeli.rethis.command.set.*
import eu.vendeli.rethis.command.sortedset.*
import eu.vendeli.rethis.command.string.*
import eu.vendeli.rethis.shared.request.common.FieldValue
import eu.vendeli.rethis.shared.response.common.MoveDirection
import eu.vendeli.rethis.shared.response.stream.ZMemberBS
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.decodeToString

class ByteStringCommandsTest : ReThisTestCtx() {

    private fun String.toBS(): ByteString = ByteString(this.encodeToByteArray())

    // ==================== String Commands ====================

    @Test
    suspend fun `test GET BS command`() {
        client.set("bsTestKey1", "testValue1")
        client.getBS("bsTestKey1").shouldNotBeNull().decodeToString() shouldBe "testValue1"
    }

    @Test
    suspend fun `test SET BS command`() {
        val value = "testValue2".toBS()
        client.setBS("bsTestKey2", value).shouldNotBeNull().decodeToString() shouldBe "OK"
        client.get("bsTestKey2") shouldBe "testValue2"
    }

    @Test
    suspend fun `test GETDEL BS command`() {
        client.set("bsTestKey3", "testValue3")
        client.getDelBS("bsTestKey3").shouldNotBeNull().decodeToString() shouldBe "testValue3"
        client.get("bsTestKey3") shouldBe null
    }

    @Test
    suspend fun `test GETEX BS command`() {
        client.set("bsTestKey4", "testValue4")
        client.getExBS("bsTestKey4").shouldNotBeNull().decodeToString() shouldBe "testValue4"
    }

    @Test
    suspend fun `test GETRANGE BS command`() {
        client.set("bsTestKey5", "testValue5")
        client.getRangeBS("bsTestKey5", 0L, 4L).decodeToString() shouldBe "testV"
    }

    // ==================== Hash Commands ====================

    @Test
    suspend fun `test HGET BS command`() {
        client.hSet("bsHashKey1", FieldValue("field1", "value1"))
        client.hGetBS("bsHashKey1", "field1").shouldNotBeNull().decodeToString() shouldBe "value1"
    }

    @Test
    suspend fun `test HGETALL BS command`() {
        client.hSet("bsHashKey2", FieldValue("field2", "value2"))
        val result = client.hGetAllBS("bsHashKey2")
        result.shouldNotBeNull()
        result["field2"].shouldNotBeNull().decodeToString() shouldBe "value2"
    }

    @Test
    suspend fun `test HRANDFIELD BS command`() {
        client.hSet("bsHashKey3", FieldValue("field3", "value3"))
        client.hRandFieldBS("bsHashKey3").shouldNotBeNull().decodeToString() shouldBe "field3"
    }

    // ==================== List Commands ====================

    @Test
    suspend fun `test LPOP BS command`() {
        client.lPush("bsListKey1", "listValue1")
        client.lPopBS("bsListKey1").shouldNotBeNull().decodeToString() shouldBe "listValue1"
    }

    @Test
    suspend fun `test RPUSH BS command`() {
        val value = "listValue2".toBS()
        client.rPushBS("bsListKey2", value) shouldBe 1L
        client.lPop("bsListKey2") shouldBe "listValue2"
    }

    @Test
    suspend fun `test LMOVE BS command`() {
        client.lPush("bsListKey3", "listValue3")
        client.lMoveBS("bsListKey3", "bsListKey4", MoveDirection.LEFT, MoveDirection.LEFT)
            .decodeToString() shouldBe "listValue3"
    }

    @Test
    suspend fun `test BLMOVE BS command`() {
        client.lPush("bsListKey5", "listValue5")
        client.blMoveBS("bsListKey5", "bsListKey6", MoveDirection.LEFT, MoveDirection.LEFT, 1.0)
            .shouldNotBeNull().decodeToString() shouldBe "listValue5"
    }

    // ==================== Set Commands ====================

    @Test
    suspend fun `test SPOP BS command`() {
        client.sAdd("bsSetKey1", "setValue1")
        client.sPopBS("bsSetKey1").shouldNotBeNull().decodeToString() shouldBe "setValue1"
    }

    @Test
    suspend fun `test SRANDMEMBER BS command`() {
        client.sAdd("bsSetKey2", "setValue2")
        client.sRandMemberBS("bsSetKey2").decodeToString() shouldBe "setValue2"
    }

    // ==================== Sorted Set Commands ====================

    @Test
    suspend fun `test ZADD BS command`() {
        val member = "zsetValue1".toBS()
        client.zAddBS("bsZSetKey1", ZMemberBS(member, 1.0)) shouldBe 1L
        client.zRandMember("bsZSetKey1") shouldBe "zsetValue1"
    }

    @Test
    suspend fun `test ZRANDMEMBER BS command`() {
        client.zAdd("bsZSetKey2", eu.vendeli.rethis.shared.response.stream.ZMember("zsetValue2", 1.0))
        client.zRandMemberBS("bsZSetKey2").decodeToString() shouldBe "zsetValue2"
    }
}
