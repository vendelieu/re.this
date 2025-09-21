package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.generic.del
import eu.vendeli.rethis.command.generic.dump
import eu.vendeli.rethis.command.generic.restore
import eu.vendeli.rethis.command.string.set
import eu.vendeli.rethis.shared.request.generic.RestoreOption
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.seconds

class RestoreCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test RESTORE command without options`() {
        client.set("testKey1", "testVal").shouldNotBeNull()
        val keyDump = client.dump("testKey1").shouldNotBeNull()
        client.del("testKey1")
        client.restore("testKey1", 0L, keyDump) shouldBe true
    }

    @Test
    suspend fun `test RESTORE command with REPLACE option`() {
        client.set("testKey2", "testVal").shouldNotBeNull()
        val keyDump = client.dump("testKey2").shouldNotBeNull()
        client.restore("testKey2", 10L, keyDump, RestoreOption.REPLACE) shouldBe true
    }

    @Test
    suspend fun `test RESTORE command with ABSTTL option`() {
        client.set("testKey3", "testVal").shouldNotBeNull()
        val keyDump = client.dump("testKey3").shouldNotBeNull()
        client.del("testKey3")
        client.restore("testKey3", 10L, keyDump, RestoreOption.ABSTTL) shouldBe true
    }

    @Test
    suspend fun `test RESTORE command with IDLETIME option`() {
        client.set("testKey4", "testVal").shouldNotBeNull()
        val keyDump = client.dump("testKey4").shouldNotBeNull()
        client.del("testKey4")
        client.restore("testKey4", 10L, keyDump, RestoreOption.IdleTime(10.seconds)) shouldBe true
    }

    @Test
    suspend fun `test RESTORE command with FREQ option`() {
        client.set("testKey5", "testVal").shouldNotBeNull()
        val keyDump = client.dump("testKey5").shouldNotBeNull()
        client.del("testKey5")
        client.restore("testKey5", 10L, keyDump, RestoreOption.Frequency(10L)) shouldBe true
    }
}
