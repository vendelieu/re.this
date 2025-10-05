package eu.vendeli.rethis.commands.server

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.server.commandDocs
import eu.vendeli.rethis.command.server.commandGetKeys
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.maps.shouldNotBeEmpty

class ServerCommandIntrospectionTest : ReThisTestCtx() {
    @Test
    suspend fun `COMMAND DOCS returns docs for known commands`() {
        val res = client.commandDocs("SET", "GET")
        res.shouldNotBeEmpty()
    }

    @Test
    suspend fun `COMMAND GETKEYS extracts keys from SET`() {
        val keys = client.commandGetKeys("SET", "k", "v")
        keys.shouldNotBeEmpty()
        keys.shouldContain("k")
    }
}
