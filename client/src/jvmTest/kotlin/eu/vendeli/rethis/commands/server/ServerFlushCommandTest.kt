package eu.vendeli.rethis.commands.server

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.server.flushAll
import eu.vendeli.rethis.command.server.flushDb
import io.kotest.matchers.shouldBe

class ServerFlushCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `FLUSHDB returns true`() {
        val res = client.flushDb()
        res shouldBe true
    }

    @Test
    suspend fun `FLUSHALL returns true`() {
        val res = client.flushAll()
        res shouldBe true
    }
}
