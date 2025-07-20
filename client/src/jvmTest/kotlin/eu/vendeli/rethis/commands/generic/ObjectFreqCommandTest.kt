package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.api.spec.common.types.ReThisException
import eu.vendeli.rethis.command.generic.objectFreq
import eu.vendeli.rethis.command.string.set
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage

class ObjectFreqCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test OBJECT FREQ command`() {
        client.set("testKey", "testVal")

        shouldThrow<ReThisException> {
            client.objectFreq("testKey")
        }.shouldHaveMessage(
            "ERR An LFU maxmemory policy is not selected, access frequency not tracked. Please note that when " +
                "switching between policies at runtime LRU and LFU data will take some time to adjust.",
        )
    }

    @Test
    suspend fun `test OBJECT FREQ command with non-existent key`() {
        client.objectFreq("nonExistentKey") shouldBe null
    }
}
