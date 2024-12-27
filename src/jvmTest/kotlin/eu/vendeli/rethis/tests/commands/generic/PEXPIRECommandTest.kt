package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.pExpire
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.types.options.UpdateStrategyOption
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class PEXPIRECommandTest : ReThisTestCtx() {
    @Test
    fun `test PEXPIRE command without options`(): Unit = runBlocking {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.pExpire("testKey", 10000L) shouldBe true
    }

    @Test
    fun `test PEXPIRE command with EXPIRE option`(): Unit = runBlocking {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.pExpire("testKey", 10000L, UpdateStrategyOption.LT) shouldBe true
    }
}
