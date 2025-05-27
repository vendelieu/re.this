package eu.vendeli.rethis.commands

import eu.vendeli.rethis.commands.*
import eu.vendeli.rethis.api.spec.common.types.PlainString
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.matchers.shouldBe

class HyperLogLogCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test PFADD command with single element`() {
        client.pfAdd("testKey1", "testElement") shouldBe true
    }

    @Test
    suspend fun `test PFADD command with multiple elements`() {
        client.pfAdd("testKey2", "testElement1", "testElement2", "testElement3") shouldBe true
    }

    @Test
    suspend fun `test PFCOUNT command with single key`() {
        client.pfAdd("testKey3", "testElement")
        client.pfCount("testKey3") shouldBe 1L
    }

    @Test
    suspend fun `test PFCOUNT command with multiple keys`() {
        client.pfAdd("testKey4", "testElement1")
        client.pfAdd("testKey5", "testElement2")
        client.pfCount("testKey4", "testKey5") shouldBe 2L
    }

    @Test
    suspend fun `test PFMERGE command with single source key`() {
        client.pfAdd("testKey8", "testElement1")
        client.pfMerge("testKey9", "testKey8") shouldBe "OK"
    }

    @Test
    suspend fun `test PFMERGE command with multiple source keys`() {
        client.pfAdd("testKey10", "testElement1")
        client.pfAdd("testKey11", "testElement2")
        client.pfMerge("testKey12", "testKey10", "testKey11") shouldBe "OK"
    }

    @Test
    suspend fun `test PFSELFTEST command`() {
        client.pfSelfTest() shouldBe PlainString("OK")
    }
}
