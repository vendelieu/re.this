package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.generic.pExpireTime
import eu.vendeli.rethis.command.string.set
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class PEXPIRETIMECommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test PEXPIRETIME command`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.pExpireTime("testKey") shouldBe -1
    }
}
