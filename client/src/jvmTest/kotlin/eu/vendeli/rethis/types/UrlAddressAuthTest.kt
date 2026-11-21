package eu.vendeli.rethis.types

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.server.aclDelUser
import eu.vendeli.rethis.command.server.aclSetUser
import eu.vendeli.rethis.command.server.aclWhoAmI
import eu.vendeli.rethis.types.common.RespVer
import io.kotest.matchers.shouldBe

class UrlAddressAuthTest : ReThisTestCtx() {
    @Test
    suspend fun `connects with ACL user and percent-encoded password from url`() {
        client.aclSetUser(USER, "on", ">$PASSWORD", "allcommands", "allkeys") shouldBe true
        try {
            val url = "redis://$USER:$ENCODED_PASSWORD@${redis.host}:${redis.firstMappedPort}/0"
            val urlClient = ReThis(url, RespVer.V3, DEFAULT_SIMPLE_TEST_CFG)
            try {
                urlClient.aclWhoAmI() shouldBe USER
            } finally {
                urlClient.close()
            }
        } finally {
            client.aclDelUser(USER)
        }
    }

    private companion object {
        const val USER = "url-auth-user"
        const val PASSWORD = "p@ss:w/rd%1"
        const val ENCODED_PASSWORD = "p%40ss%3Aw%2Frd%251"
    }
}
