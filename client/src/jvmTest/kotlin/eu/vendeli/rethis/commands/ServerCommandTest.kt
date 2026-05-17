package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.server.commandDocs
import eu.vendeli.rethis.command.server.commandInfo
import eu.vendeli.rethis.command.server.commandList
import eu.vendeli.rethis.command.server.configGet
import eu.vendeli.rethis.command.server.configResetStat
import eu.vendeli.rethis.command.server.configSet
import eu.vendeli.rethis.command.server.dbSize
import eu.vendeli.rethis.command.server.info
import eu.vendeli.rethis.command.server.lolwut
import eu.vendeli.rethis.shared.request.string.KeyValue
import eu.vendeli.rethis.shared.types.RType
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldNotBe

class ServerCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test DBSIZE command`() {
        client.dbSize() shouldBeGreaterThanOrEqual 0L
    }

    @Test
    suspend fun `test CONFIG RESETSTAT command`() {
        client.configResetStat().shouldBeTrue()
    }

    @Test
    suspend fun `test CONFIG GET command`() {
        client.configGet("maxmemory") shouldNotBe RType.Null
    }

    @Test
    suspend fun `test CONFIG SET command`() {
        client.configSet(KeyValue("maxmemory-policy", "allkeys-lru")).shouldBeTrue()
        client.configSet(KeyValue("maxmemory-policy", "noeviction")).shouldBeTrue()
    }

    @Test
    suspend fun `test INFO command`() {
        client.info().isNotEmpty().shouldBeTrue()
    }

    @Test
    suspend fun `test INFO command with section`() {
        client.info("server").isNotEmpty().shouldBeTrue()
    }

    @Test
    suspend fun `test LOLWUT command`() {
        client.lolwut().isNotEmpty().shouldBeTrue()
    }

    @Test
    suspend fun `test COMMAND INFO command`() {
        client.commandInfo("GET") shouldNotBe RType.Null
    }

    @Test
    suspend fun `test COMMAND LIST command`() {
        client.commandList().shouldHaveAtLeastSize(50)
    }

    @Test
    suspend fun `test COMMAND DOCS command`() {
        client.commandDocs("GET") shouldNotBe RType.Null
    }
}
