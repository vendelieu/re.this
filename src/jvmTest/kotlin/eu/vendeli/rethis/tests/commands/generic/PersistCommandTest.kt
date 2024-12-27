package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.expire
import eu.vendeli.rethis.commands.persist
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.commands.ttl
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class PersistCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test PERSIST command`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.expire("testKey", 10L).shouldBe(true)
        client.persist("testKey") shouldBe true
        client.ttl("testKey") shouldBe -1
    }
}
