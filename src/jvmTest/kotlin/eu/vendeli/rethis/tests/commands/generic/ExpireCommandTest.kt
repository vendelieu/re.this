package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.expire
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.types.options.UpdateStrategyOption
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ExpireCommandTest : ReThisTestCtx() {
    @Test
    fun `test EXPIRE command without options`(): Unit = runTest {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.expire("testKey", 10L) shouldBe true
    }

    @Test
    fun `test EXPIRE command with EXPIRE option`(): Unit = runTest {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.expire("testKey", 10L, UpdateStrategyOption.LT) shouldBe true
    }
}
