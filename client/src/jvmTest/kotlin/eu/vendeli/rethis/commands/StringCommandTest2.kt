package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.codecs.string.DelExCommandCodec
import eu.vendeli.rethis.command.string.*
import eu.vendeli.rethis.shared.request.string.DelExOption
import eu.vendeli.rethis.shared.request.string.GetExOption
import io.kotest.core.annotation.EnabledIf
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.io.Buffer
import kotlinx.io.readString
import kotlin.time.Duration.Companion.seconds

class StringCommandTest2 : ReThisTestCtx() {
    @Test
    suspend fun `test APPEND command`() {
        val origin = "testValue10"
        val appending = "newValue10"
        client.set("testKey10", origin)
        client.append("testKey10", appending) shouldBe (origin.length + appending.length)
    }

    @Test
    suspend fun `test DECR command`() {
        client.set("testKey11", "10")
        client.decr("testKey11") shouldBe 9L
    }

    @Test
    suspend fun `test DECRBY command`() {
        client.set("testKey12", "10")
        client.decrBy("testKey12", 5) shouldBe 5L
    }

    @Test
    suspend fun `test GET command`() {
        Buffer()
        client.set("testKey13", "testValue13")
        client.get("testKey13") shouldBe "testValue13"
    }

    @Test
    suspend fun `test GETDEL command`() {
        client.set("testKey14", "testValue14")
        client.getDel("testKey14") shouldBe "testValue14"
    }

    @Test
    suspend fun `test GETEX command`() {
        client.set("testKey15", "testValue15")
        client.getEx("testKey15", GetExOption.Ex(10.seconds)) shouldBe "testValue15"
    }

    @Test
    suspend fun `test GETRANGE command`() {
        client.set("testKey16", "testValue16")
        client.getRange("testKey16", 0L, 4) shouldBe "testV"
    }

    @Test
    suspend fun `test INCR command`() {
        client.set("testKey17", "10")
        client.incr("testKey17") shouldBe 11L
    }

    @Test
    suspend fun `test INCRBY command`() {
        client.set("testKey18", "10")
        client.incrBy("testKey18", 5) shouldBe 15L
    }

    @Test
    suspend fun `test INCRBYFLOAT command`() {
        client.set("testKey19", "10.5")
        client.incrByFloat("testKey19", 5.5) shouldBe 16.0
    }

    @Test
    suspend fun `big string test`() {
        val key = "test:big:string"
        val bigValue = "A".repeat(1024 * 500)

        client.set(key, bigValue)
        client.get(key) shouldBe bigValue
    }

    @Test
    suspend fun `test DELEX with IFDEQ and IFDNE takes the hex digest from DIGEST`() {
        client.set("delexDigestKey", "v")
        val digest = client.digest("delexDigestKey").shouldNotBeNull()

        client.delEx("delexDigestKey", DelExOption.IfDigestNe(digest)) shouldBe false
        client.get("delexDigestKey") shouldBe "v"

        client.delEx("delexDigestKey", DelExOption.IfDigestEq(digest)) shouldBe true
        client.get("delexDigestKey") shouldBe null
    }

    @Test
    suspend fun `test DELEX codec writes the digest verbatim as hex text`() {
        DelExCommandCodec
            .encode(defaultCharset, "k", DelExOption.IfDigestEq("00ab12cd34ef5678"))
            .data
            .readString() shouldBe
            "*4\r\n\$5\r\nDELEX\r\n\$1\r\nk\r\n\$5\r\nIFDEQ\r\n\$16\r\n00ab12cd34ef5678\r\n"
        DelExCommandCodec
            .encode(defaultCharset, "k", DelExOption.IfDigestNe("00ab12cd34ef5678"))
            .data
            .readString() shouldBe
            "*4\r\n\$5\r\nDELEX\r\n\$1\r\nk\r\n\$5\r\nIFDNE\r\n\$16\r\n00ab12cd34ef5678\r\n"
    }
}
