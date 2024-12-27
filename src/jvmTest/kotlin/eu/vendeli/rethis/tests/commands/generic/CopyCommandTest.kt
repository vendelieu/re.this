package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.copy
import eu.vendeli.rethis.commands.del
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.types.options.CopyOption
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class CopyCommandTest : ReThisTestCtx() {
    @Test
    fun `test COPY command without options`(): Unit = runBlocking {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.del("destKey")
        client.copy("testKey", "destKey") shouldBe true
    }

    @Test
    fun `test COPY command with REPLACE option`(): Unit = runBlocking {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.copy("testKey", "destKey", CopyOption.REPLACE) shouldBe true
    }

    @Test
    fun `test COPY command with DB option`(): Unit = runBlocking {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.copy("testKey", "destKey", CopyOption.DB(1), CopyOption.REPLACE) shouldBe true
    }
}
