package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.objectRefCount
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import io.ktor.utils.io.*

class ObjectRefCountCommandTest : ReThisTestCtx() {
    @Test
    fun `test OBJECT REFCOUNT command`(): Unit = runBlocking {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.objectRefCount("testKey") shouldBe 1L
    }
}
