package eu.vendeli.rethis.commands

import eu.vendeli.rethis.IOSensitiveTest
import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.string.get
import eu.vendeli.rethis.command.string.set
import io.kotest.core.annotation.EnabledIf
import io.kotest.matchers.shouldBe

@EnabledIf(IOSensitiveTest::class)
class SensitiveCommand : ReThisTestCtx() {
    @Test
    suspend fun `big string test`() {
        val key = "test:big:string"
        val bigValue = "A".repeat(1024 * 500)

        client.set(key, bigValue)
        client.get(key) shouldBe bigValue
    }
}
