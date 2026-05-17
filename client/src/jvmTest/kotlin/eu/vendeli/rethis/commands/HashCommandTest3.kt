package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.hash.hGetDel
import eu.vendeli.rethis.command.hash.hGetEx
import eu.vendeli.rethis.command.hash.hPExpire
import eu.vendeli.rethis.command.hash.hPExpireAt
import eu.vendeli.rethis.command.hash.hPExpireTime
import eu.vendeli.rethis.command.hash.hSet
import eu.vendeli.rethis.command.hash.hSetEx
import eu.vendeli.rethis.shared.request.common.FieldValue
import eu.vendeli.rethis.shared.request.hash.HFieldValue
import eu.vendeli.rethis.shared.request.hash.HSetExOption
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.seconds

class HashCommandTest3 : ReThisTestCtx() {
    @Test
    suspend fun `test HGETDEL command`() {
        client.hSet("hgetdelKey", FieldValue("f1", "v1"))
        client.hGetDel("hgetdelKey", "f1") shouldBe listOf("v1")
    }

    @Test
    suspend fun `test HGETEX command`() {
        client.hSet("hgetexKey", FieldValue("f2", "v2"))
        client.hGetEx("hgetexKey", null, "f2") shouldBe listOf("v2")
    }

    @Test
    suspend fun `test HSETEX command`() {
        client
            .hSetEx(
                "hsetexKey",
                null,
                HSetExOption.Expiration.Ex(60.seconds),
                HFieldValue("f3", "v3"),
            ).shouldBeTrue()
    }

    @Test
    suspend fun `test HPEXPIRE command`() {
        client.hSet("hpexpireKey", FieldValue("f4", "v4"))
        client.hPExpire("hpexpireKey", 5.seconds, null, "f4") shouldBe listOf(1L)
    }

    @Test
    suspend fun `test HPEXPIREAT command`() {
        client.hSet("hpexpireatKey", FieldValue("f5", "v5"))
        client.hPExpireAt("hpexpireatKey", timestamp.plus(60.seconds), null, "f5") shouldBe listOf(1L)
    }

    @Test
    suspend fun `test HPEXPIRETIME command`() {
        client.hSet("hpexpiretimeKey", FieldValue("f6", "v6"))
        client.hPExpireTime("hpexpiretimeKey", "f6") shouldBe listOf(-1L)
    }
}
