package eu.vendeli.rethis.utils

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.shared.decoders.aggregate.ArrayByteArrayDecoder
import eu.vendeli.rethis.shared.decoders.aggregate.ArrayLongDecoder
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.ResponseParsingException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer

class ArrayDecodersNullableTest : TestCtx() {
    private fun buffer(payload: String): Buffer = Buffer().apply { writeFully(payload.encodeToByteArray()) }

    @Test
    suspend fun `long array decodeNullable reads every element with resp3 null`() {
        val input = buffer("*3\r\n:1\r\n_\r\n:3\r\n")

        ArrayLongDecoder.decodeNullable(input, defaultCharset) shouldBe listOf(1L, null, 3L)
        input.exhausted() shouldBe true
    }

    @Test
    suspend fun `long array decodeNullable reads every element with resp2 null`() {
        val input = buffer("*3\r\n:1\r\n\$-1\r\n:3\r\n")

        ArrayLongDecoder.decodeNullable(input, defaultCharset) shouldBe listOf(1L, null, 3L)
        input.exhausted() shouldBe true
    }

    @Test
    suspend fun `long array decodeNullable rejects non integer token`() {
        val ex = shouldThrow<ResponseParsingException> {
            ArrayLongDecoder.decodeNullable(buffer("*1\r\n+x\r\n"), defaultCharset)
        }

        ex.message!! shouldContain "integer or null"
    }

    @Test
    suspend fun `byte array decodeNullable handles null element without pre-consumed code`() {
        val input = buffer("*2\r\n\$1\r\na\r\n_\r\n")

        val result = ArrayByteArrayDecoder.decodeNullable(input, defaultCharset, null)

        result.map { it?.decodeToString() } shouldBe listOf("a", null)
        input.exhausted() shouldBe true
    }

    @Test
    suspend fun `byte array decodeNullable handles null element with pre-consumed array code`() {
        val input = buffer("*2\r\n\$1\r\na\r\n_\r\n")
        input.readByte() // the caller already consumed `*`

        val result = ArrayByteArrayDecoder.decodeNullable(input, defaultCharset, RespCode.ARRAY)

        result.map { it?.decodeToString() } shouldBe listOf("a", null)
        input.exhausted() shouldBe true
    }
}
