package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.expire
import eu.vendeli.rethis.commands.persist
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.commands.ttl
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class PersistCommandTest : ReThisTestCtx() {
    @Test
    fun `test PERSIST command`(): Unit = runTest {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.expire("testKey", 10L).shouldBe(true)
        client.persist("testKey") shouldBe true
        client.ttl("testKey") shouldBe -1
    }
}
