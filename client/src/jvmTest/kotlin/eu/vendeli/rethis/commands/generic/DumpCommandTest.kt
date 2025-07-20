package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.generic.dump
import eu.vendeli.rethis.command.string.set
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain

class DumpCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test DUMP command`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client
            .dump("testKey")
            .shouldNotBeNull()
            .shouldContain("testVal")
    }
}
