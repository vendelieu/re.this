package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.json.jsonGetBA
import eu.vendeli.rethis.command.json.jsonSet
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class JsonByteArrayCommandsTest : ReThisTestCtx(withJsonModule = true) {

    // ==================== JSON Commands ====================

    @Test
    suspend fun `test JSON_GET BA command`() {
        client.jsonSet("baJsonKey1", "[1, 2, 3]", ".")
        client.jsonGetBA("baJsonKey1").shouldNotBeNull().decodeToString() shouldBe "[1,2,3]"
    }
}
