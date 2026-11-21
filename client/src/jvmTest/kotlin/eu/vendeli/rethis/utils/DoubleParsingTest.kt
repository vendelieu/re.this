package eu.vendeli.rethis.utils

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.codecs.sortedset.ZScoreCommandCodec
import eu.vendeli.rethis.shared.decoders.general.DoubleDecoder
import eu.vendeli.rethis.shared.decoders.geospatial.GeoPosDecoder
import eu.vendeli.rethis.shared.decoders.sortedset.ZPopResultDecoder
import eu.vendeli.rethis.shared.types.F64
import eu.vendeli.rethis.shared.types.ResponseParsingException
import eu.vendeli.rethis.shared.utils.readResponseWrapped
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer

class DoubleParsingTest : TestCtx() {
    private suspend fun buffer(payload: String): Buffer = Buffer {
        writeFully(payload.encodeToByteArray())
    }

    @Test
    suspend fun `DoubleDecoder decodes positive infinity`() {
        DoubleDecoder.decode(buffer(",inf\r\n"), defaultCharset) shouldBe Double.POSITIVE_INFINITY
    }

    @Test
    suspend fun `DoubleDecoder decodes negative infinity`() {
        DoubleDecoder.decode(buffer(",-inf\r\n"), defaultCharset) shouldBe Double.NEGATIVE_INFINITY
    }

    @Test
    suspend fun `DoubleDecoder decodes nan`() {
        DoubleDecoder.decode(buffer(",nan\r\n"), defaultCharset).isNaN().shouldBeTrue()
    }

    @Test
    suspend fun `DoubleDecoder still decodes plain doubles`() {
        DoubleDecoder.decode(buffer(",3.14\r\n"), defaultCharset) shouldBe 3.14
    }

    @Test
    suspend fun `readResponseWrapped reads infinity double`() {
        buffer(",inf\r\n").readResponseWrapped(defaultCharset) shouldBe F64(Double.POSITIVE_INFINITY)
    }

    @Test
    suspend fun `readResponseWrapped rejects garbage double with ResponseParsingException`() {
        shouldThrow<ResponseParsingException> {
            buffer(",abc\r\n").readResponseWrapped(defaultCharset)
        }
    }

    @Test
    suspend fun `ZPopResultDecoder decodes resp2 inf score`() {
        val result = ZPopResultDecoder.decode(buffer("*3\r\n\$1\r\nk\r\n\$1\r\nm\r\n\$3\r\ninf\r\n"), defaultCharset)
        result?.score shouldBe Double.POSITIVE_INFINITY
    }

    @Test
    suspend fun `GeoPosDecoder decodes inf coordinates`() {
        val payload = "*1\r\n*2\r\n\$3\r\ninf\r\n\$4\r\n-inf\r\n"
        val position = GeoPosDecoder.decode(buffer(payload), defaultCharset).single()!!.single()
        position.longitude shouldBe Double.POSITIVE_INFINITY
        position.latitude shouldBe Double.NEGATIVE_INFINITY
    }

    @Test
    suspend fun `ZScore codec decodes resp2 bulk inf score`() {
        ZScoreCommandCodec.decode(buffer("\$3\r\ninf\r\n"), defaultCharset) shouldBe Double.POSITIVE_INFINITY
    }
}
