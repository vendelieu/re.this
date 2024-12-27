package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.pExpireTime
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class PEXPIRETIMECommandTest : ReThisTestCtx() {
    @Test
    fun `test PEXPIRETIME command`(): Unit = runTest {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.pExpireTime("testKey") shouldBe -1
    }
}
