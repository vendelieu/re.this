package eu.vendeli.rethis.utils

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.shared.decoders.aggregate.XReadGroupDecoder
import eu.vendeli.rethis.shared.types.BulkString
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.ResponseParsingException
import eu.vendeli.rethis.shared.types.stream.XReadGroupMessage
import eu.vendeli.rethis.shared.types.stream.XReadGroupResponse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer

class XReadGroupDecoderTest : TestCtx() {
    private fun buffer(payload: String): Buffer = Buffer().apply { writeFully(payload.encodeToByteArray()) }

    private val expected = listOf(
        XReadGroupResponse("s", listOf(XReadGroupMessage("1-0", mapOf("f" to BulkString("v"))))),
    )

    @Test
    suspend fun `decodeNullable returns null for resp3 null without code`() {
        XReadGroupDecoder.decodeNullable(buffer("_\r\n"), defaultCharset, null).shouldBeNull()
    }

    @Test
    suspend fun `decodeNullable returns null for resp3 null with pre-consumed code`() {
        val input = buffer("_\r\n")
        input.readByte()

        XReadGroupDecoder.decodeNullable(input, defaultCharset, RespCode.NULL).shouldBeNull()
        input.exhausted() shouldBe true
    }

    @Test
    suspend fun `decodeNullable returns null for resp2 null array`() {
        val input = buffer("*-1\r\n+OK\r\n")

        XReadGroupDecoder.decodeNullable(input, defaultCharset, null).shouldBeNull()
        input.readByte() shouldBe '+'.code.toByte()
    }

    @Test
    suspend fun `decodeNullable returns null for resp2 null array with pre-consumed code`() {
        val input = buffer("*-1\r\n")
        input.readByte()

        XReadGroupDecoder.decodeNullable(input, defaultCharset, RespCode.ARRAY).shouldBeNull()
        input.exhausted() shouldBe true
    }

    @Test
    suspend fun `decode keeps returning an empty list for resp2 null array`() {
        XReadGroupDecoder.decode(buffer("*-1\r\n"), defaultCharset, null).shouldBeEmpty()
    }

    @Test
    suspend fun `decodeNullable returns empty list for empty array`() {
        XReadGroupDecoder.decodeNullable(buffer("*0\r\n"), defaultCharset, null)!!.shouldBeEmpty()
    }

    @Test
    suspend fun `decode throws on corrupt size line`() {
        shouldThrow<ResponseParsingException> {
            XReadGroupDecoder.decode(buffer("*x\r\n"), defaultCharset, null)
        }
    }

    @Test
    suspend fun `decode throws on stream entry without messages`() {
        shouldThrow<ResponseParsingException> {
            XReadGroupDecoder.decode(buffer("*1\r\n*1\r\n\$1\r\ns\r\n"), defaultCharset, null)
        }
    }

    @Test
    suspend fun `decode throws on malformed message entry`() {
        shouldThrow<ResponseParsingException> {
            XReadGroupDecoder.decode(
                buffer("*1\r\n*2\r\n\$1\r\ns\r\n*1\r\n*1\r\n\$3\r\n1-0\r\n"),
                defaultCharset,
                null,
            )
        }
    }

    @Test
    suspend fun `decode reads resp2 single entry reply`() {
        val reply = "*1\r\n*2\r\n\$1\r\ns\r\n*1\r\n*2\r\n\$3\r\n1-0\r\n*2\r\n\$1\r\nf\r\n\$1\r\nv\r\n"

        XReadGroupDecoder.decode(buffer(reply), defaultCharset, null) shouldBe expected
    }

    @Test
    suspend fun `decode reads resp3 single entry reply`() {
        val reply = "%1\r\n\$1\r\ns\r\n*1\r\n*2\r\n\$3\r\n1-0\r\n%1\r\n\$1\r\nf\r\n\$1\r\nv\r\n"

        XReadGroupDecoder.decode(buffer(reply), defaultCharset, null) shouldBe expected
    }
}
