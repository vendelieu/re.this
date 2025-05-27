package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.commands.expire
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.types.options.UpdateStrategyOption
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class ExpireCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test EXPIRE command without options`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.expire("testKey", 10L) shouldBe true
    }

    @Test
    suspend fun `test EXPIRE command with EXPIRE option`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.expire("testKey", 10L, UpdateStrategyOption.LT) shouldBe true
    }
}
