package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.codecs.generic.PExpireCommandCodec
import eu.vendeli.rethis.command.generic.pExpire
import eu.vendeli.rethis.command.generic.pTtl
import eu.vendeli.rethis.command.string.set
import eu.vendeli.rethis.shared.request.common.UpdateStrategyOption
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.longs.shouldBeBetween
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.io.readString
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class PEXPIRECommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test PEXPIRE command without options`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.pExpire("testKey", 10.seconds) shouldBe true
    }

    @Test
    suspend fun `test PEXPIRE command with EXPIRE option`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.pExpire("testKey", 10.seconds, UpdateStrategyOption.LT) shouldBe true
    }

    @Test
    suspend fun `test PEXPIRE encodes the duration in milliseconds`() {
        val wire = PExpireCommandCodec.encode(Charsets.UTF_8, "k", 10.seconds, null).data.readString()
        wire shouldContain "\$5\r\n10000\r\n"
    }

    @Test
    suspend fun `test PEXPIRE sets the TTL in milliseconds`() {
        client.set("pexpireKey", "v").shouldNotBeNull()
        client.pExpire("pexpireKey", 10.seconds) shouldBe true
        client.pTtl("pexpireKey").shouldBeBetween(9000L, 10000L)

        client.set("pexpireKey2", "v").shouldNotBeNull()
        client.pExpire("pexpireKey2", 500.milliseconds) shouldBe true
        client.pTtl("pexpireKey2") shouldBeGreaterThan 0L
    }
}
