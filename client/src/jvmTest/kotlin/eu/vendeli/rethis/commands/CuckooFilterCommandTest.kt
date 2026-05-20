package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.cf.cfAdd
import eu.vendeli.rethis.command.cf.cfCount
import eu.vendeli.rethis.command.cf.cfDel
import eu.vendeli.rethis.command.cf.cfExists
import eu.vendeli.rethis.command.cf.cfMExists
import eu.vendeli.rethis.command.cf.cfReserve
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class CuckooFilterCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test CF_RESERVE and CF_ADD command`() {
        client.cfReserve("cfKey1", 100L, null, null, null) shouldBe "OK"
        client.cfAdd("cfKey1", "hello") shouldBe true
        client.cfExists("cfKey1", "hello") shouldBe true
        client.cfExists("cfKey1", "missing") shouldBe false
    }

    @Test
    suspend fun `test CF_COUNT command`() {
        client.cfReserve("cfKey2", 100L, null, null, null) shouldBe "OK"
        client.cfAdd("cfKey2", "item")
        client.cfAdd("cfKey2", "item")
        client.cfCount("cfKey2", "item") shouldBe 2L
    }

    @Test
    suspend fun `test CF_DEL and CF_MEXISTS commands`() {
        client.cfReserve("cfKey3", 100L, null, null, null) shouldBe "OK"
        client.cfAdd("cfKey3", "a")
        client.cfMExists("cfKey3", "a", "b") shouldNotBe null
        client.cfDel("cfKey3", "a") shouldBe true
    }
}
