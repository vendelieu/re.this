package eu.vendeli.rethis.commands.server

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.server.*
import eu.vendeli.rethis.shared.types.RArray
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import io.kotest.matchers.types.shouldBeTypeOf

class ServerAclCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `ACL LIST returns a list`() {
        client.aclList().any { it.startsWith("user default ") } shouldBe true
    }

    @Test
    suspend fun `ACL USERS returns users`() {
        client.aclUsers() shouldContain "default"
    }

    @Test
    suspend fun `ACL WHOAMI returns current user`() {
        client.aclWhoAmI() shouldBe "default"
    }

    @Test
    suspend fun `ACL GENPASS generates password`() {
        client.aclGenPass() shouldMatch Regex("[0-9a-f]{64}")
        client.aclGenPass(bits = 16) shouldMatch Regex("[0-9a-f]{4}")
    }

    @Test
    suspend fun `ACL CAT returns categories`() {
        client.aclCat() shouldContain "read"
    }

    @Test
    suspend fun `ACL LOG returns entries or empty list`() {
        client.aclLog().shouldBeTypeOf<RArray>()
    }

    @Test
    suspend fun `ACL GETUSER default exists`() {
        client.aclGetUser("default").shouldNotBeNull() shouldContainKey "flags"
        client.aclGetUser("no-such-user-xyz") shouldBe null
    }
}
