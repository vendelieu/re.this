package eu.vendeli.rethis.commands.server

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.server.aclDelUser
import eu.vendeli.rethis.command.server.aclSetUser
import eu.vendeli.rethis.command.server.aclUsers
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe

class ServerAclMutationCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `ACL SETUSER can create a temporary user`() {
        try {
            val res = client.aclSetUser(TMP_USER, "on", "nopass", "allcommands", "allkeys")
            res shouldBe true
            client.aclUsers() shouldContain TMP_USER
        } finally {
            client.aclDelUser(TMP_USER)
        }

        client.aclUsers() shouldNotContain TMP_USER
    }

    private companion object {
        const val TMP_USER = "tmp-user"
    }
}
