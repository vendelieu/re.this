package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.commands.type
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class TypeCommandTest : ReThisTestCtx() {
    @Test
    fun `test TYPE command`(): Unit = runTest {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.type("testKey") shouldBe "string"
    }

    @Test
    fun `test TYPE command with non-existent key`(): Unit = runTest {
        client.type("nonExistentKey") shouldBe "none"
    }
}
