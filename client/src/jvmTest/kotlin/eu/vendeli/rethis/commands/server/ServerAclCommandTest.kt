package eu.vendeli.rethis.commands.server

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.server.*
import io.kotest.matchers.shouldNotBe

class ServerAclCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `ACL LIST returns a list`() {
        val res = client.aclList()
        res shouldNotBe null
    }

    @Test
    suspend fun `ACL USERS returns users`() {
        val res = client.aclUsers()
        res shouldNotBe null
    }

    @Test
    suspend fun `ACL WHOAMI returns current user`() {
        val res = client.aclWhoAmI()
        res shouldNotBe null
    }

    @Test
    suspend fun `ACL GENPASS generates password`() {
        val res = client.aclGenPass()
        res shouldNotBe null
    }

    @Test
    suspend fun `ACL CAT returns categories`() {
        val res = client.aclCat()
        res shouldNotBe null
    }

    @Test
    suspend fun `ACL LOG returns entries or empty list`() {
        val res = client.aclLog()
        res shouldNotBe null
    }

    @Test
    suspend fun `ACL GETUSER default exists`() {
        val res = client.aclGetUser("default")
        res shouldNotBe null
    }
}
