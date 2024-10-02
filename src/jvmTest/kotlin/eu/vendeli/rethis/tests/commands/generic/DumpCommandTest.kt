package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.dump
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain

class DumpCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test DUMP command`() {
        client.set("testKey", "testVal").shouldNotBeNull()
        client
            .dump("testKey")
            .shouldNotBeNull()
            .decodeToString()
            .shouldContain("testVal")
    }
}
