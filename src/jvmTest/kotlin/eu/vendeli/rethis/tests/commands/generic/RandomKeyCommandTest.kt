package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.randomKey
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class RandomKeyCommandTest : ReThisTestCtx() {
    @Test
    fun `test RANDOMKEY command`(): Unit = runBlocking {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.randomKey().shouldNotBeNull()
    }
}
