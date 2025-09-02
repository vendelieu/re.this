package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.generic.objectIdleTime
import eu.vendeli.rethis.command.string.set
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class ObjectIdleTimeCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test OBJECT IDLETIME command`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.objectIdleTime("testKey") shouldBe 0L
    }
}
