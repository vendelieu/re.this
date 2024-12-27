package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.del
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class DelCommandTest : ReThisTestCtx() {
    @Test
    fun `test DEL command with single key`(): Unit = runBlocking {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.del("testKey") shouldBe 1L
    }

    @Test
    fun `test DEL command with multiple keys`(): Unit = runBlocking {
        client.set("testKey1", "testVal1").shouldNotBeNull()
        client.set("testKey2", "testVal2").shouldNotBeNull()
        client.del("testKey1", "testKey2") shouldBe 2L
    }
}
