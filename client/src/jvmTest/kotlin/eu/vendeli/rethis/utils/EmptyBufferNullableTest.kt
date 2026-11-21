package eu.vendeli.rethis.utils

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.shared.decoders.general.BulkByteArrayDecoder
import eu.vendeli.rethis.shared.decoders.general.BulkStringDecoder
import eu.vendeli.rethis.shared.decoders.general.SimpleStringDecoder
import eu.vendeli.rethis.shared.decoders.general.VerbatimStringDecoder
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer

class EmptyBufferNullableTest : TestCtx() {
    private fun buffer(payload: String): Buffer = Buffer().apply { writeFully(payload.encodeToByteArray()) }

    @Test
    suspend fun `bulk string decodeNullable returns null on empty buffer`() {
        BulkStringDecoder.decodeNullable(EMPTY_BUFFER, defaultCharset).shouldBeNull()
    }

    @Test
    suspend fun `simple string decodeNullable returns null on empty buffer`() {
        SimpleStringDecoder.decodeNullable(EMPTY_BUFFER, defaultCharset).shouldBeNull()
    }

    @Test
    suspend fun `verbatim string decodeNullable returns null on empty buffer`() {
        VerbatimStringDecoder.decodeNullable(EMPTY_BUFFER, defaultCharset, null).shouldBeNull()
    }

    @Test
    suspend fun `bulk byte array decodeNullable returns null on empty buffer`() {
        BulkByteArrayDecoder.decodeNullable(EMPTY_BUFFER, defaultCharset).shouldBeNull()
    }

    @Test
    suspend fun `bulk string decodeNullable still decodes real replies`() {
        BulkStringDecoder.decodeNullable(buffer("$-1\r\n"), defaultCharset).shouldBeNull()
        BulkStringDecoder.decodeNullable(buffer("$2\r\nhi\r\n"), defaultCharset) shouldBe "hi"
    }
}
