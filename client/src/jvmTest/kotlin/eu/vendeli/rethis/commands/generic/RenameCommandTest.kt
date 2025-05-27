package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.commands.rename
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class RenameCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test RENAME command`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.rename("testKey", "newKey2") shouldBe true
    }
}
