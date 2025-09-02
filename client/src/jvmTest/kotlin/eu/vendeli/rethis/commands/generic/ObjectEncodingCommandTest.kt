package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.generic.objectEncoding
import eu.vendeli.rethis.command.string.set
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class ObjectEncodingCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test OBJECT ENCODING command`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.objectEncoding("testKey") shouldBe "embstr"
    }
}
