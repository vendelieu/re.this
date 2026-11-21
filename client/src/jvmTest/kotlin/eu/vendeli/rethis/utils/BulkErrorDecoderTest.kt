package eu.vendeli.rethis.utils

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.shared.decoders.general.BulkErrorDecoder
import eu.vendeli.rethis.shared.types.ReThisException
import eu.vendeli.rethis.shared.types.RespCode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer

class BulkErrorDecoderTest : TestCtx() {
    private fun frame(payload: String) = "!${payload.encodeToByteArray().size}\r\n$payload\r\n"

    private suspend fun bufferOf(raw: String): Buffer = Buffer {
        writeFully(raw.encodeToByteArray())
    }

    @Test
    suspend fun `throws server message when type byte was already skipped`() {
        val buffer = bufferOf("21\r\nSYNTAX invalid syntax\r\n")

        val ex = shouldThrow<ReThisException> {
            BulkErrorDecoder.decode(buffer, defaultCharset, RespCode.BULK_ERROR)
        }

        ex.message shouldBe "SYNTAX invalid syntax"
    }

    @Test
    suspend fun `throws server message when decoder resolves the type byte itself`() {
        val buffer = bufferOf("!21\r\nSYNTAX invalid syntax\r\n")

        val ex = shouldThrow<ReThisException> {
            BulkErrorDecoder.decode(buffer, defaultCharset, null)
        }

        ex.message shouldBe "SYNTAX invalid syntax"
    }

    @Test
    suspend fun `consumes the whole frame so a following frame stays parseable`() {
        val buffer = bufferOf(frame("ERR first") + "+OK\r\n")

        shouldThrow<ReThisException> { BulkErrorDecoder.decode(buffer, defaultCharset, null) }

        buffer.readByte().toInt().toChar() shouldBe '+'
        buffer.readByte().toInt().toChar() shouldBe 'O'
    }

    @Test
    suspend fun `buffer is exhausted after the only frame is decoded`() {
        val buffer = bufferOf(frame("ERR only"))

        shouldThrow<ReThisException> { BulkErrorDecoder.decode(buffer, defaultCharset, null) }

        buffer.exhausted() shouldBe true
    }

    @Test
    suspend fun `declared length is a byte count for multibyte payload`() {
        val payload = "ERR тест-😀"
        val buffer = bufferOf(frame(payload))

        val ex = shouldThrow<ReThisException> {
            BulkErrorDecoder.decode(buffer, defaultCharset, null)
        }

        ex.message shouldBe payload
        buffer.exhausted() shouldBe true
    }
}
