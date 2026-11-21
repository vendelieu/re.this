package eu.vendeli.rethis.commands.generic

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.generic.objectFreq
import eu.vendeli.rethis.command.string.set
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.ReThisException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeTypeOf

class ObjectFreqCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test OBJECT FREQ command`() {
        client.set("testKey", "testVal")

        shouldThrow<ReThisException> {
            throw client.objectFreq("testKey").shouldBeTypeOf<RType.Error>().exception
        }.message.shouldNotBeNull() shouldStartWith "ERR An LFU maxmemory policy"
    }

    @Test
    suspend fun `test OBJECT FREQ command with non-existent key`() {
        client.objectFreq("nonExistentKey") shouldBe RType.Null
    }
}
