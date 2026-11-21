package eu.vendeli.rethis.utils

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.shared.decoders.general.BulkErrorDecoder
import eu.vendeli.rethis.shared.decoders.general.BulkStringDecoder
import eu.vendeli.rethis.shared.decoders.general.VerbatimStringDecoder
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.ResponseParsingException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer

class SizedTextTest : TestCtx() {
    private fun buffer(vararg parts: ByteArray): Buffer = Buffer().apply { parts.forEach { writeFully(it) } }

    private fun buffer(payload: String): Buffer = buffer(payload.encodeToByteArray())

    @Test
    suspend fun `bulk string with a declared size beyond the buffered bytes is a parsing failure`() {
        shouldThrow<ResponseParsingException> {
            BulkStringDecoder.decode(buffer("10\r\nabc\r\n"), defaultCharset, RespCode.BULK)
        }
    }

    @Test
    suspend fun `bulk string with a declared size missing its trailing CRLF is a parsing failure`() {
        shouldThrow<ResponseParsingException> {
            BulkStringDecoder.decode(buffer("3\r\nabc"), defaultCharset, RespCode.BULK)
        }
    }

    @Test
    suspend fun `bulk error with a negative size is a parsing failure`() {
        shouldThrow<ResponseParsingException> {
            BulkErrorDecoder.decode(buffer("-5\r\n"), defaultCharset, RespCode.BULK_ERROR)
        }
    }

    @Test
    suspend fun `bulk error with a declared size beyond the buffered bytes is a parsing failure`() {
        shouldThrow<ResponseParsingException> {
            BulkErrorDecoder.decode(buffer("10\r\nERR\r\n"), defaultCharset, RespCode.BULK_ERROR)
        }
    }

    @Test
    suspend fun `verbatim string with a declared size beyond the buffered bytes is a parsing failure`() {
        shouldThrow<ResponseParsingException> {
            VerbatimStringDecoder.decode(buffer("10\r\ntxt:ab\r\n"), defaultCharset, RespCode.VERBATIM_STRING)
        }
    }

    @Test
    suspend fun `bulk string of exact size consumes only its payload and CRLF`() {
        val input = buffer("3\r\nabc\r\n+OK\r\n")

        BulkStringDecoder.decode(input, defaultCharset, RespCode.BULK) shouldBe "abc"
        input.size shouldBe 5L
    }

    @Test
    suspend fun `bulk string with a non-utf8 charset still decodes multi-byte payloads`() {
        val bytes = "café".toByteArray(Charsets.ISO_8859_1)
        val input = buffer("${bytes.size}\r\n".encodeToByteArray(), bytes, "\r\n".encodeToByteArray())

        BulkStringDecoder.decode(input, Charsets.ISO_8859_1, RespCode.BULK) shouldBe "café"
        input.exhausted() shouldBe true
    }
}
