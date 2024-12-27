package eu.vendeli.rethis.tests.commands

import eu.vendeli.rethis.commands.*
import eu.vendeli.rethis.types.core.PlainString
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class HyperLogLogCommandTest : ReThisTestCtx() {
    @Test
    fun `test PFADD command with single element`(): Unit = runTest {
        client.pfAdd("testKey1", "testElement") shouldBe true
    }

    @Test
    fun `test PFADD command with multiple elements`(): Unit = runTest {
        client.pfAdd("testKey2", "testElement1", "testElement2", "testElement3") shouldBe true
    }

    @Test
    fun `test PFCOUNT command with single key`(): Unit = runTest {
        client.pfAdd("testKey3", "testElement")
        client.pfCount("testKey3") shouldBe 1L
    }

    @Test
    fun `test PFCOUNT command with multiple keys`(): Unit = runTest {
        client.pfAdd("testKey4", "testElement1")
        client.pfAdd("testKey5", "testElement2")
        client.pfCount("testKey4", "testKey5") shouldBe 2L
    }

    @Test
    fun `test PFMERGE command with single source key`(): Unit = runTest {
        client.pfAdd("testKey8", "testElement1")
        client.pfMerge("testKey9", "testKey8") shouldBe "OK"
    }

    @Test
    fun `test PFMERGE command with multiple source keys`(): Unit = runTest {
        client.pfAdd("testKey10", "testElement1")
        client.pfAdd("testKey11", "testElement2")
        client.pfMerge("testKey12", "testKey10", "testKey11") shouldBe "OK"
    }

    @Test
    fun `test PFSELFTEST command`(): Unit = runTest {
        client.pfSelfTest() shouldBe PlainString("OK")
    }
}
