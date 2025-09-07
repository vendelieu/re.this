package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.generic.objectFreq
import eu.vendeli.rethis.command.string.set
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.ReThisException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.kotest.matchers.types.shouldBeTypeOf

class ObjectFreqCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test OBJECT FREQ command`() {
        client.set("testKey", "testVal")

        shouldThrow<ReThisException> {
            throw client.objectFreq("testKey").shouldBeTypeOf<RType.Error>().exception
        }.shouldHaveMessage(
            "ERR An LFU maxmemory policy is not selected, access frequency not tracked. Please note that when " +
                "switching between policies at runtime LRU and LFU data will take some time to adjust.",
        )
    }

    @Test
    suspend fun `test OBJECT FREQ command with non-existent key`() {
        client.objectFreq("nonExistentKey") shouldBe RType.Null
    }
}
