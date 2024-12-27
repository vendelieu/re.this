package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.rename
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class RenameCommandTest : ReThisTestCtx() {
    @Test
    fun `test RENAME command`(): Unit = runBlocking {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.rename("testKey", "newKey2") shouldBe "OK"
    }
}
