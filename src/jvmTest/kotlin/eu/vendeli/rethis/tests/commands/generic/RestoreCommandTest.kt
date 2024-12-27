package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.restore
import eu.vendeli.rethis.types.options.RestoreOption
import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.commands.del
import eu.vendeli.rethis.commands.dump
import eu.vendeli.rethis.commands.set
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class RestoreCommandTest : ReThisTestCtx() {
    @Test
    fun `test RESTORE command without options`(): Unit = runTest {
        client.set("testKey1", "testVal").shouldNotBeNull()
        val keyDump = client.dump("testKey1").shouldNotBeNull()
        client.del("testKey1")
        client.restore("testKey1", 0L, keyDump) shouldBe "OK"
    }

    @Test
    fun `test RESTORE command with REPLACE option`(): Unit = runTest {
        client.set("testKey2", "testVal").shouldNotBeNull()
        val keyDump = client.dump("testKey2").shouldNotBeNull()
        client.restore("testKey2", 10L, keyDump, RestoreOption.REPLACE) shouldBe "OK"
    }

    @Test
    fun `test RESTORE command with ABSTTL option`(): Unit = runTest {
        client.set("testKey3", "testVal").shouldNotBeNull()
        val keyDump = client.dump("testKey3").shouldNotBeNull()
        client.del("testKey3")
        client.restore("testKey3", 10L, keyDump, RestoreOption.ABSTTL) shouldBe "OK"
    }

    @Test
    fun `test RESTORE command with IDLETIME option`(): Unit = runTest {
        client.set("testKey4", "testVal").shouldNotBeNull()
        val keyDump = client.dump("testKey4").shouldNotBeNull()
        client.del("testKey4")
        client.restore("testKey4", 10L, keyDump, RestoreOption.IDLETIME(10.seconds)) shouldBe "OK"
    }

    @Test
    fun `test RESTORE command with FREQ option`(): Unit = runTest {
        client.set("testKey5", "testVal").shouldNotBeNull()
        val keyDump = client.dump("testKey5").shouldNotBeNull()
        client.del("testKey5")
        client.restore("testKey5", 10L, keyDump, RestoreOption.FREQ(10L)) shouldBe "OK"
    }
}
