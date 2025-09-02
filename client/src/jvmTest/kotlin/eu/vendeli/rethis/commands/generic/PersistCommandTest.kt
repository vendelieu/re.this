package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.generic.expire
import eu.vendeli.rethis.command.generic.persist
import eu.vendeli.rethis.command.generic.ttl
import eu.vendeli.rethis.command.string.set
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.seconds

class PersistCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test PERSIST command`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.expire("testKey", 10.seconds).shouldBe(true)
        client.persist("testKey") shouldBe true
        client.ttl("testKey") shouldBe -1
    }
}
