package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.dump
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class DumpCommandTest : ReThisTestCtx() {
    @Test
    fun `test DUMP command`(): Unit = runBlocking {
        client.set("testKey", "testVal").shouldNotBeNull()
        client
            .dump("testKey")
            .shouldNotBeNull()
            .toString(Charsets.UTF_8)
            .shouldContain("testVal")
    }
}
