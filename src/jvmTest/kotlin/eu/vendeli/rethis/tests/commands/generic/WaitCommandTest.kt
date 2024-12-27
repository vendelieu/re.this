package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.commands.wait
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class WaitCommandTest : ReThisTestCtx() {
    @Test
    fun `test WAIT command`(): Unit = runBlocking {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.wait(1L, 1000L) shouldBe 0L
    }
}
