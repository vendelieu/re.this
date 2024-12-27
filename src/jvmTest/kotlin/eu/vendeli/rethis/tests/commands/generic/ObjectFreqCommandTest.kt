package eu.vendeli.rethis.tests.commands.generic

import eu.vendeli.rethis.ReThisException
import eu.vendeli.rethis.commands.objectFreq
import eu.vendeli.rethis.commands.set
import eu.vendeli.rethis.ReThisTestCtx
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import io.kotest.matchers.throwable.shouldHaveMessage

class ObjectFreqCommandTest : ReThisTestCtx() {
    @Test
    fun `test OBJECT FREQ command`(): Unit = runTest {
        client.set("testKey", "testVal")

        shouldThrow<ReThisException> {
            client.objectFreq("testKey")
        }.shouldHaveMessage(
            "ERR An LFU maxmemory policy is not selected, access frequency not tracked. Please note that when " +
                "switching between policies at runtime LRU and LFU data will take some time to adjust.",
        )
    }

    @Test
    fun `test OBJECT FREQ command with non-existent key`(): Unit = runTest {
        client.objectFreq("nonExistentKey") shouldBe null
    }
}
