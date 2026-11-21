package eu.vendeli.rethis.utils

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.shared.types.PlainString
import eu.vendeli.rethis.shared.types.RespProtocolException
import eu.vendeli.rethis.shared.utils.readCompleteResponseInto
import eu.vendeli.rethis.shared.utils.readResponseWrapped
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer

class RespFramingEdgeCaseTest : TestCtx() {
    private suspend fun frame(payload: String): String {
        val target = Buffer()
        ByteReadChannel(payload.encodeToByteArray()).readCompleteResponseInto(target)
        return target.readText()
    }

    private fun buffer(payload: String): Buffer = Buffer().apply { writeFully(payload.encodeToByteArray()) }

    @Test
    suspend fun `framer rejects bare minus in aggregate header`() {
        shouldThrowExactly<RespProtocolException> { frame("*-\r\n") }
    }

    @Test
    suspend fun `framer rejects repeated minus in aggregate header`() {
        shouldThrowExactly<RespProtocolException> { frame("*--5\r\n") }
    }

    @Test
    suspend fun `framer rejects bulk header without digits`() {
        shouldThrowExactly<RespProtocolException> { frame("$\r\n") }
    }

    @Test
    suspend fun `framer still frames null array`() {
        frame("*-1\r\n") shouldBe "*-1\r\n"
    }

    @Test
    suspend fun `framer still frames empty array`() {
        frame("*0\r\n") shouldBe "*0\r\n"
    }

    @Test
    suspend fun `framer still frames bulk string`() {
        frame("$5\r\nhello\r\n") shouldBe "$5\r\nhello\r\n"
    }

    @Test
    suspend fun `readResponseWrapped accepts CR CR LF terminator and keeps following frame`() {
        val input = buffer("+ab\r\r\n+next\r\n")

        input.readResponseWrapped(defaultCharset) shouldBe PlainString("ab\r")
        input.readResponseWrapped(defaultCharset) shouldBe PlainString("next")
    }
}
