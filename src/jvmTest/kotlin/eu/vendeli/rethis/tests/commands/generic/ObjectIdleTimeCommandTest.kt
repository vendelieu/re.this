package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.objectIdleTime
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ObjectIdleTimeCommandTest : ReThisTestCtx() {
    @Test
    fun `test OBJECT IDLETIME command`(): Unit = runTest {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.objectIdleTime("testKey") shouldBe 0L
    }
}
