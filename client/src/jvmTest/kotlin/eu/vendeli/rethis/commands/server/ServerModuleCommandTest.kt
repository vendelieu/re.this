package eu.vendeli.rethis.commands.server

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.server.moduleList
import io.kotest.matchers.shouldNotBe

class ServerModuleCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `MODULE LIST returns modules`() {
        val res = client.moduleList()
        res shouldNotBe null
    }
}
