package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.exists
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ExistsCommandTest : ReThisTestCtx() {
    @Test
    fun `test EXISTS command with single key`(): Unit = runTest {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.exists("testKey") shouldBe 1L
    }

    @Test
    fun `test EXISTS command with multiple keys`(): Unit = runTest {
        client.set("testKey1", "testVal1").shouldNotBeNull()
        client.set("testKey2", "testVal2").shouldNotBeNull()
        client.exists("testKey1", "testKey2") shouldBe 2L
    }
}
