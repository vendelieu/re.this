package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.search.ftCreate
import eu.vendeli.rethis.command.search.ftDropIndex
import eu.vendeli.rethis.command.search.ftList
import eu.vendeli.rethis.command.search.ftSugAdd
import eu.vendeli.rethis.command.search.ftSugLen
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class RediSearchCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test FT_CREATE and FT__LIST commands`() {
        client.ftCreate("idx-1", "ON", "HASH", "PREFIX", "1", "doc:", "SCHEMA", "title", "TEXT") shouldBe "OK"
        client.ftList() shouldNotBe null
        client.ftDropIndex("idx-1", null) shouldBe "OK"
    }

    @Test
    suspend fun `test FT_SUGADD and FT_SUGLEN commands`() {
        client.ftSugAdd("sugKey1", "hello", 1.0, null, null) shouldBe 1L
        client.ftSugAdd("sugKey1", "world", 1.0, null, null) shouldBe 2L
        client.ftSugLen("sugKey1") shouldBe 2L
    }
}
