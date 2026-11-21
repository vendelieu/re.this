package eu.vendeli.rethis.utils

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.shared.decoders.sortedset.ZPopResultDecoder
import eu.vendeli.rethis.shared.response.sortedset.ZPopResult
import eu.vendeli.rethis.shared.types.ResponseParsingException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer

class ZPopResultDecoderTest : TestCtx() {
    private suspend fun decode(payload: String): ZPopResult? = ZPopResultDecoder.decode(
        Buffer { writeFully(payload.encodeToByteArray()) },
        defaultCharset,
    )

    @Test
    suspend fun `decodes a RESP2 null array to null`() {
        decode("*-1\r\n") shouldBe null
    }

    @Test
    suspend fun `decodes a RESP3 null to null`() {
        decode("_\r\n") shouldBe null
    }

    @Test
    suspend fun `decodes a key, member and score array`() {
        decode("*3\r\n\$1\r\nk\r\n\$1\r\nm\r\n\$1\r\n1\r\n") shouldBe ZPopResult("k", "m", 1.0)
    }

    @Test
    suspend fun `decodes a RESP3 double score`() {
        decode("*3\r\n\$1\r\nk\r\n\$1\r\nm\r\n,1.5\r\n") shouldBe ZPopResult("k", "m", 1.5)
    }

    @Test
    suspend fun `rejects an array with the wrong number of elements`() {
        shouldThrow<ResponseParsingException> {
            decode("*2\r\n\$1\r\nk\r\n\$1\r\nm\r\n")
        }
    }

    @Test
    suspend fun `rejects a non-array reply`() {
        shouldThrow<ResponseParsingException> {
            decode(":1\r\n")
        }
    }
}
