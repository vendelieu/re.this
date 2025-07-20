package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.generic.objectRefCount
import eu.vendeli.rethis.command.string.set
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class ObjectRefCountCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test OBJECT REFCOUNT command`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.objectRefCount("testKey") shouldBe 1L
    }
}
