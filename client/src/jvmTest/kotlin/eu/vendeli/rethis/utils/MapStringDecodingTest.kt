package eu.vendeli.rethis.utils

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.codecs.hash.HGetAllCommandCodec
import eu.vendeli.rethis.shared.decoders.aggregate.ArrayStringDecoder
import eu.vendeli.rethis.shared.decoders.aggregate.MapStringDecoder
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer

class MapStringDecodingTest : TestCtx() {
    private fun bulk(value: String) = "\$${value.encodeToByteArray().size}\r\n$value\r\n"

    private fun resp2HashReply(vararg fields: Pair<String, String>): String = buildString {
        append("*${fields.size * 2}\r\n")
        fields.forEach { (key, value) ->
            append(bulk(key))
            append(bulk(value))
        }
    }

    private fun resp3HashReply(vararg fields: Pair<String, String>): String = buildString {
        append("%${fields.size}\r\n")
        fields.forEach { (key, value) ->
            append(bulk(key))
            append(bulk(value))
        }
    }

    private suspend fun replyBuffer(payload: String): Buffer = Buffer {
        writeFully(payload.encodeToByteArray())
    }

    @Test
    suspend fun `hGetAll decodes resp2 hash with large bulk string value`() {
        val big = "x".repeat(30_000)
        val reply = replyBuffer(resp2HashReply("small1" to "a", "big" to big, "small2" to "b"))

        HGetAllCommandCodec.decode(reply, defaultCharset) shouldContainExactly
            mapOf("small1" to "a", "big" to big, "small2" to "b")
    }

    @Test
    suspend fun `hGetAll decodes resp3 hash with large bulk string value`() {
        val big = "x".repeat(30_000)
        val reply = replyBuffer(resp3HashReply("small1" to "a", "big" to big, "small2" to "b"))

        HGetAllCommandCodec.decode(reply, defaultCharset) shouldContainExactly
            mapOf("small1" to "a", "big" to big, "small2" to "b")
    }

    @Test
    suspend fun `hGetAll decodes value containing CRLF and LF`() {
        val multiline = "line1\r\nline2\nline3"
        val reply = replyBuffer(resp2HashReply("first" to multiline, "second" to "plain"))

        HGetAllCommandCodec.decode(reply, defaultCharset) shouldContainExactly
            mapOf("first" to multiline, "second" to "plain")
    }

    @Test
    suspend fun `hGetAll decodes multibyte value whose byte size exceeds char count`() {
        val multibyte = "тест-значение-😀"
        val reply = replyBuffer(resp2HashReply("key" to multibyte, "tail" to "b"))

        HGetAllCommandCodec.decode(reply, defaultCharset) shouldContainExactly
            mapOf("key" to multibyte, "tail" to "b")
    }

    @Test
    suspend fun `nullable map decode handles resp2 null value`() {
        val reply = replyBuffer("*4\r\n${bulk("k")}\$-1\r\n${bulk("j")}${bulk("v")}")

        HGetAllCommandCodec.decode(reply, defaultCharset) shouldContainExactly
            mapOf("k" to null, "j" to "v")
    }

    @Test
    suspend fun `nullable map decode handles resp3 null value`() {
        val reply = replyBuffer("%2\r\n${bulk("k")}_\r\n${bulk("j")}${bulk("v")}")

        HGetAllCommandCodec.decode(reply, defaultCharset) shouldContainExactly
            mapOf("k" to null, "j" to "v")
    }

    @Test
    suspend fun `non-nullable map decode handles large and multiline values`() {
        val big = "y".repeat(40_000)
        val multiline = "a\r\nb"
        val reply = replyBuffer(resp2HashReply("big" to big, "ml" to multiline))

        MapStringDecoder.decode(reply, defaultCharset, null) shouldContainExactly
            mapOf("big" to big, "ml" to multiline)
    }

    @Test
    suspend fun `nullable array decode consumes resp3 null element fully`() {
        val reply = replyBuffer("*3\r\n${bulk("a")}_\r\n${bulk("b")}")

        ArrayStringDecoder.decodeNullable(reply, defaultCharset) shouldBe listOf("a", null, "b")
    }
}
