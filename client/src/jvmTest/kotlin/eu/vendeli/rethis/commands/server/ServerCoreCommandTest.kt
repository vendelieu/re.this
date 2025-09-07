package eu.vendeli.rethis.commands.server

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.server.*
import io.kotest.matchers.shouldNotBe

class ServerCoreCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `COMMAND COUNT returns a number`() {
        val res = client.commandCount()
        res shouldNotBe null
    }

    @Test
    suspend fun `TIME returns server time`() {
        val res = client.time()
        res shouldNotBe null
    }

    @Test
    suspend fun `ROLE returns role info`() {
        val res = client.role()
        res shouldNotBe null
    }

    @Test
    suspend fun `LASTSAVE returns timestamp`() {
        val res = client.lastSave()
        res shouldNotBe null
    }

    @Test
    suspend fun `SLOWLOG LEN returns length`() {
        val len = client.slowLogLen()
        len shouldNotBe null
    }

    @Test
    suspend fun `SLOWLOG GET returns entries`() {
        val res = client.slowLogGet()
        res shouldNotBe null
    }

    @Test
    suspend fun `SLOWLOG RESET resets log`() {
        val res = client.slowLogReset()
        res shouldNotBe null
    }
}
