package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.commands.touch
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class TouchCommandTest : ReThisTestCtx() {
    @Test
    fun `test TOUCH command with single key`(): Unit = runTest {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.touch("testKey") shouldBe 1L
    }

    @Test
    fun `test TOUCH command with multiple keys`(): Unit = runTest {
        client.set("testKey1", "testVal1").shouldNotBeNull()
        client.set("testKey2", "testVal2").shouldNotBeNull()
        client.touch("testKey1", "testKey2") shouldBe 2L
    }
}
