package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.commands.copy
import eu.vendeli.rethis.commands.del
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.types.options.CopyOption
import eu.vendeli.rethis.ReThisTestCtx
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
