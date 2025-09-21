package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.generic.copy
import eu.vendeli.rethis.command.generic.del
import eu.vendeli.rethis.command.string.set
import eu.vendeli.rethis.shared.request.generic.CopyOption
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class CopyCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test COPY command without options`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.del("destKey")
        client.copy("testKey", "destKey") shouldBe true
    }

    @Test
    suspend fun `test COPY command with REPLACE option`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.copy("testKey", "destKey", CopyOption.REPLACE) shouldBe true
    }

    @Test
    suspend fun `test COPY command with DB option`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.copy("testKey", "destKey", CopyOption.DB(1), CopyOption.REPLACE) shouldBe true
    }
}
