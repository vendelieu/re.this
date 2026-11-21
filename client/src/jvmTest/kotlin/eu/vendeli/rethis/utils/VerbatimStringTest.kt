package eu.vendeli.rethis.utils

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.shared.decoders.general.VerbatimStringDecoder
import eu.vendeli.rethis.shared.types.PlainString
import eu.vendeli.rethis.shared.types.ResponseParsingException
import eu.vendeli.rethis.shared.types.VerbatimString
import eu.vendeli.rethis.shared.utils.readResponseWrapped
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer

class VerbatimStringTest : TestCtx() {
    private suspend fun buffer(payload: String): Buffer = Buffer {
        writeFully(payload.encodeToByteArray())
    }

    @Test
    suspend fun `decode strips the format prefix`() {
        VerbatimStringDecoder.decode(buffer("=15\r\ntxt:Some string\r\n"), defaultCharset, null) shouldBe "Some string"
    }

    @Test
    suspend fun `decodeNullable strips the format prefix`() {
        VerbatimStringDecoder.decodeNullable(buffer("=15\r\ntxt:Some string\r\n"), defaultCharset, null) shouldBe
            "Some string"
    }

    @Test
    suspend fun `decodeNullable returns null for null frame`() {
        VerbatimStringDecoder.decodeNullable(buffer("=-1\r\n"), defaultCharset, null).shouldBeNull()
    }

    @Test
    suspend fun `decode keeps multi-line content intact`() {
        VerbatimStringDecoder.decode(buffer("=16\r\ntxt:line1\r\nline2\r\n"), defaultCharset, null) shouldBe
            "line1\r\nline2"
    }

    @Test
    suspend fun `decode rejects frame shorter than the format prefix`() {
        shouldThrow<ResponseParsingException> {
            VerbatimStringDecoder.decode(buffer("=3\r\ntxt\r\n"), defaultCharset, null)
        }
    }

    @Test
    suspend fun `readResponseWrapped reads multi-line verbatim string by length`() {
        val input = buffer("=16\r\ntxt:line1\r\nline2\r\n")

        input.readResponseWrapped(defaultCharset) shouldBe VerbatimString("txt", "line1\r\nline2")
        input.exhausted() shouldBe true
    }

    @Test
    suspend fun `readResponseWrapped keeps following frame intact after verbatim string`() {
        val input = buffer("=16\r\ntxt:line1\r\nline2\r\n+OK\r\n")

        input.readResponseWrapped(defaultCharset) shouldBe VerbatimString("txt", "line1\r\nline2")
        input.readResponseWrapped(defaultCharset) shouldBe PlainString("OK")
    }

    @Test
    suspend fun `readResponseWrapped rejects frame shorter than the format prefix`() {
        shouldThrow<ResponseParsingException> {
            buffer("=3\r\ntxt\r\n").readResponseWrapped(defaultCharset)
        }
    }
}
