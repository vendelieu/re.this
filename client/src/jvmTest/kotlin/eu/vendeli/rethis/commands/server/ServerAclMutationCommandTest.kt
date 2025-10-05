package eu.vendeli.rethis.commands.server

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.server.aclSetUser
import io.kotest.matchers.shouldBe

class ServerAclMutationCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `ACL SETUSER can create a temporary user`() {
        val res = client.aclSetUser("tmp-user", "on", "nopass", "allcommands", "allkeys")
        res shouldBe true
    }
}
