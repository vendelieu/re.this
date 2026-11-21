package eu.vendeli.rethis.utils

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.codecs.bitmap.BitfieldCommandCodec
import eu.vendeli.rethis.codecs.list.LPopCountCommandCodec
import eu.vendeli.rethis.codecs.list.LRangeCommandCodec
import eu.vendeli.rethis.codecs.transaction.ExecCommandCodec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer

// RESP2 null array (`*-1`) versus RESP3 null (`_`) and the empty array (`*0`).
class NullArrayDecodingTest : TestCtx() {
    private val charset = Charsets.UTF_8

    private suspend fun bufferOf(raw: String): Buffer = Buffer {
        writeFully(raw.encodeToByteArray())
    }

    @Test
    suspend fun `spec declaring NULL decodes a RESP2 null array to null`() {
        LPopCountCommandCodec.decode(bufferOf("*-1\r\n"), charset) shouldBe null
        ExecCommandCodec.decode(bufferOf("*-1\r\n"), charset) shouldBe null
        BitfieldCommandCodec.decode(bufferOf("*-1\r\n"), charset) shouldBe null
    }

    @Test
    suspend fun `spec declaring NULL decodes a RESP3 null to null`() {
        LPopCountCommandCodec.decode(bufferOf("_\r\n"), charset) shouldBe null
        ExecCommandCodec.decode(bufferOf("_\r\n"), charset) shouldBe null
    }

    @Test
    suspend fun `spec declaring NULL keeps an empty array empty`() {
        LPopCountCommandCodec.decode(bufferOf("*0\r\n"), charset)?.shouldBeEmpty()
        ExecCommandCodec.decode(bufferOf("*0\r\n"), charset)?.shouldBeEmpty()
    }

    @Test
    suspend fun `spec declaring NULL still decodes a populated array`() {
        LPopCountCommandCodec.decode(bufferOf("*2\r\n\$1\r\na\r\n\$1\r\nb\r\n"), charset) shouldBe listOf("a", "b")
    }

    @Test
    suspend fun `spec without NULL keeps a RESP2 null array as an empty list`() {
        LRangeCommandCodec.decode(bufferOf("*-1\r\n"), charset).shouldBeEmpty()
    }
}
