package eu.vendeli.rethis.utils

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.shared.decoders.string.LcsDecoder
import eu.vendeli.rethis.shared.response.string.LcsResult
import eu.vendeli.rethis.shared.response.string.LcsResult.LcsMatch
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer

class LcsDecoderTest : TestCtx() {
    private suspend fun decode(payload: String): LcsResult = LcsDecoder.decode(
        Buffer { writeFully(payload.encodeToByteArray()) },
        defaultCharset,
    )

    // wire bytes captured from a live server for LCS o1 o2 IDX (o1 = xxabcyyy, o2 = abczzzyyy)
    private val matches = "*2\r\n*2\r\n*2\r\n:5\r\n:7\r\n*2\r\n:6\r\n:8\r\n*2\r\n*2\r\n:2\r\n:4\r\n*2\r\n:0\r\n:2\r\n"
    private val matchesWithLen =
        "*2\r\n*3\r\n*2\r\n:5\r\n:7\r\n*2\r\n:6\r\n:8\r\n:3\r\n*3\r\n*2\r\n:2\r\n:4\r\n*2\r\n:0\r\n:2\r\n:3\r\n"
    private val tail = "\$3\r\nlen\r\n:6\r\n"

    private val expected = listOf(
        LcsMatch(5L..7L, 6L..8L),
        LcsMatch(2L..4L, 0L..2L),
    )
    private val expectedWithLen = expected.map { it.copy(length = 3L) }

    @Test
    suspend fun `keeps both ranges of a RESP2 match`() {
        decode("*4\r\n\$7\r\nmatches\r\n$matches$tail") shouldBe LcsResult(expected, 6L)
    }

    @Test
    suspend fun `keeps both ranges and the length of a RESP2 match with WITHMATCHLEN`() {
        decode("*4\r\n\$7\r\nmatches\r\n$matchesWithLen$tail") shouldBe LcsResult(expectedWithLen, 6L)
    }

    @Test
    suspend fun `keeps both ranges of a RESP3 match`() {
        decode("%2\r\n\$7\r\nmatches\r\n$matches$tail") shouldBe LcsResult(expected, 6L)
    }

    @Test
    suspend fun `keeps both ranges and the length of a RESP3 match with WITHMATCHLEN`() {
        decode("%2\r\n\$7\r\nmatches\r\n$matchesWithLen$tail") shouldBe LcsResult(expectedWithLen, 6L)
    }

    @Test
    suspend fun `decodes an empty match list`() {
        decode("*4\r\n\$7\r\nmatches\r\n*0\r\n$tail") shouldBe LcsResult(emptyList(), 6L)
    }
}
