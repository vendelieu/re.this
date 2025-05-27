package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.commands.objectEncoding
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class ObjectEncodingCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test OBJECT ENCODING command`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.objectEncoding("testKey") shouldBe "embstr"
    }
}
