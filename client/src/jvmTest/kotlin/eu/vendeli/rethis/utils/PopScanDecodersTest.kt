package eu.vendeli.rethis.utils

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.codecs.list.BlmPopCommandCodec
import eu.vendeli.rethis.codecs.list.LmPopCommandCodec
import eu.vendeli.rethis.codecs.sortedset.BzMPopCommandCodec
import eu.vendeli.rethis.codecs.sortedset.ZMPopCommandCodec
import eu.vendeli.rethis.shared.decoders.common.LMPopDecoder
import eu.vendeli.rethis.shared.decoders.common.LPopDecoder
import eu.vendeli.rethis.shared.decoders.common.PairScanDecoder
import eu.vendeli.rethis.shared.decoders.common.StringScanDecoder
import eu.vendeli.rethis.shared.response.common.MPopResult
import eu.vendeli.rethis.shared.response.common.PopResult
import eu.vendeli.rethis.shared.response.common.ScanResult
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.ResponseParsingException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer

class PopScanDecodersTest : TestCtx() {
    private fun buffer(payload: String): Buffer = Buffer().apply { writeFully(payload.encodeToByteArray()) }

    private fun preConsumed(payload: String): Buffer = buffer(payload).also { it.readByte() }

    private val lPopReply = "*2\r\n\$1\r\nk\r\n\$1\r\nv\r\n"
    private val lmPopReply = "*2\r\n\$1\r\nk\r\n*1\r\n\$1\r\na\r\n"
    private val stringScanReply = "*2\r\n\$1\r\n0\r\n*1\r\n\$1\r\na\r\n"
    private val pairScanReply = "*2\r\n\$1\r\n0\r\n*2\r\n\$1\r\na\r\n\$1\r\nb\r\n"

    @Test
    suspend fun `lpop returns null for resp3 null`() {
        LPopDecoder.decode(buffer("_\r\n"), defaultCharset, null) shouldBe null
    }

    @Test
    suspend fun `lpop returns null for resp2 null array`() {
        LPopDecoder.decode(buffer("*-1\r\n"), defaultCharset, null) shouldBe null
    }

    @Test
    suspend fun `lpop throws on empty array`() {
        shouldThrow<ResponseParsingException> { LPopDecoder.decode(buffer("*0\r\n"), defaultCharset, null) }
    }

    @Test
    suspend fun `lpop honours pre-consumed code`() {
        LPopDecoder.decode(buffer(lPopReply), defaultCharset, null) shouldBe PopResult("k", "v")
        LPopDecoder.decode(preConsumed(lPopReply), defaultCharset, RespCode.ARRAY) shouldBe PopResult("k", "v")
    }

    @Test
    suspend fun `lmpop returns null for resp3 null and resp2 null array`() {
        LMPopDecoder.decode(buffer("_\r\n"), defaultCharset, null) shouldBe null
        LMPopDecoder.decode(buffer("*-1\r\n"), defaultCharset, null) shouldBe null
    }

    @Test
    suspend fun `the multi pop commands decode a null reply as null on both protocols`() {
        listOf("_\r\n", "*-1\r\n").forEach { reply ->
            LmPopCommandCodec.decode(buffer(reply), defaultCharset) shouldBe null
            BlmPopCommandCodec.decode(buffer(reply), defaultCharset) shouldBe null
            ZMPopCommandCodec.decode(buffer(reply), defaultCharset) shouldBe null
            BzMPopCommandCodec.decode(buffer(reply), defaultCharset) shouldBe null
        }
    }

    @Test
    suspend fun `lmpop throws on dangling element`() {
        shouldThrow<ResponseParsingException> {
            LMPopDecoder.decode(buffer("*1\r\n\$1\r\nk\r\n"), defaultCharset, null)
        }
    }

    @Test
    suspend fun `lmpop honours pre-consumed code`() {
        val expected = listOf(MPopResult("k", listOf("a")))

        LMPopDecoder.decode(buffer(lmPopReply), defaultCharset, null) shouldBe expected
        LMPopDecoder.decode(preConsumed(lmPopReply), defaultCharset, RespCode.ARRAY) shouldBe expected
    }

    @Test
    suspend fun `string scan throws on short reply`() {
        shouldThrow<ResponseParsingException> {
            StringScanDecoder.decode(buffer("*1\r\n\$1\r\n0\r\n"), defaultCharset, null)
        }
    }

    @Test
    suspend fun `string scan honours pre-consumed code`() {
        val expected = ScanResult("0", listOf("a"))

        StringScanDecoder.decode(buffer(stringScanReply), defaultCharset, null) shouldBe expected
        StringScanDecoder.decode(preConsumed(stringScanReply), defaultCharset, RespCode.ARRAY) shouldBe expected
    }

    @Test
    suspend fun `scan decoders reject null replies with a parsing exception`() {
        listOf("_\r\n", "*-1\r\n").forEach { reply ->
            shouldThrow<ResponseParsingException> { StringScanDecoder.decode(buffer(reply), defaultCharset, null) }
            shouldThrow<ResponseParsingException> { PairScanDecoder.decode(buffer(reply), defaultCharset, null) }
        }
    }

    @Test
    suspend fun `pair scan throws on short reply`() {
        shouldThrow<ResponseParsingException> {
            PairScanDecoder.decode(buffer("*1\r\n\$1\r\n0\r\n"), defaultCharset, null)
        }
    }

    @Test
    suspend fun `pair scan throws on odd length chunk`() {
        shouldThrow<ResponseParsingException> {
            PairScanDecoder.decode(
                buffer("*2\r\n\$1\r\n0\r\n*3\r\n\$1\r\na\r\n\$1\r\nb\r\n\$1\r\nc\r\n"),
                defaultCharset,
                null,
            )
        }
    }

    @Test
    suspend fun `pair scan honours pre-consumed code`() {
        val expected = ScanResult("0", listOf("a" to "b"))

        PairScanDecoder.decode(buffer(pairScanReply), defaultCharset, null) shouldBe expected
        PairScanDecoder.decode(preConsumed(pairScanReply), defaultCharset, RespCode.ARRAY) shouldBe expected
    }
}
