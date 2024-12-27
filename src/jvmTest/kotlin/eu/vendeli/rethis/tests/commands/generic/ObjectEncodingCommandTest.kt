package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.commands.objectEncoding
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class ObjectEncodingCommandTest : ReThisTestCtx() {
    @Test
    fun `test OBJECT ENCODING command`(): Unit = runTest {
        client.set("testKey", "testVal").shouldNotBeNull()
        client.objectEncoding("testKey") shouldBe "embstr"
    }
}
