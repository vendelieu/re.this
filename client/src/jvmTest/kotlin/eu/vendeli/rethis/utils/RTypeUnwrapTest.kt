package eu.vendeli.rethis.utils

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.shared.types.Int64
import eu.vendeli.rethis.shared.types.PlainString
import eu.vendeli.rethis.shared.types.Push
import eu.vendeli.rethis.shared.types.VerbatimString
import eu.vendeli.rethis.shared.types.RArray
import eu.vendeli.rethis.shared.types.RMap
import eu.vendeli.rethis.shared.types.RSet
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.ResponseParsingException
import eu.vendeli.rethis.shared.utils.readResponseWrapped
import eu.vendeli.rethis.shared.utils.unwrap
import eu.vendeli.rethis.shared.utils.unwrapList
import eu.vendeli.rethis.shared.utils.unwrapRESPAgnosticMap
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.utils.io.core.*

class RTypeUnwrapTest : TestCtx() {
    private val configGetReply = "*2\r\n\$16\r\nmaxmemory-policy\r\n\$7\r\nnoevict\r\n"

    @Test
    suspend fun `unwrap on array reply fails loudly pointing to unwrapList`() {
        val reply = Buffer {
            writeFully(configGetReply.encodeToByteArray())
        }.readResponseWrapped()

        shouldThrow<ResponseParsingException> {
            reply.unwrap<List<String>>()
        }.message shouldContain "unwrapList"
    }

    @Test
    suspend fun `unwrap on set reply fails loudly pointing to unwrapSet`() {
        val reply = RSet(setOf(PlainString("a")))

        shouldThrow<ResponseParsingException> {
            reply.unwrap<Set<String>>()
        }.message shouldContain "unwrapSet"
    }

    @Test
    suspend fun `unwrap on map reply fails loudly pointing to unwrapMap`() {
        val reply = RMap(mapOf(PlainString("k") to PlainString("v")))

        shouldThrow<ResponseParsingException> {
            reply.unwrap<Map<String, String>>()
        }.message shouldContain "unwrapMap"
    }

    @Test
    suspend fun `unwrap on push reply fails loudly pointing to unwrapList`() {
        val reply = Push(listOf(PlainString("message")))

        shouldThrow<ResponseParsingException> {
            reply.unwrap<String>()
        }.message shouldContain "unwrapList"
    }

    @Test
    suspend fun `unwrap to RType returns aggregate as is`() {
        val reply = RArray(listOf(PlainString("a")))

        reply.unwrap<RType>() shouldBe reply
    }

    @Test
    suspend fun `unwrap on null reply returns null without failure`() {
        RType.Null.unwrap<String>().shouldBeNull()
    }

    @Test
    suspend fun `pair array reply unwraps with resp agnostic map`() {
        val reply = Buffer {
            writeFully(configGetReply.encodeToByteArray())
        }.readResponseWrapped()

        reply.unwrapRESPAgnosticMap<String, String>() shouldBe mapOf("maxmemory-policy" to "noevict")
    }

    @Test
    suspend fun `odd length pair array fails instead of producing a bogus entry`() {
        val reply = Buffer {
            writeFully("*3\r\n\$2\r\nk1\r\n\$2\r\nv1\r\n\$2\r\nk2\r\n".encodeToByteArray())
        }.readResponseWrapped()

        shouldThrow<ResponseParsingException> {
            reply.unwrapRESPAgnosticMap<String, String>()
        }
    }

    @Test
    suspend fun `even pair array and resp3 map still unwrap to a map`() {
        val array = RArray(listOf(PlainString("k1"), PlainString("v1"), PlainString("k2"), PlainString("v2")))
        val map = RMap(mapOf(PlainString("k1") to PlainString("v1")))

        array.unwrapRESPAgnosticMap<String, String>() shouldBe mapOf("k1" to "v1", "k2" to "v2")
        map.unwrapRESPAgnosticMap<String, String>() shouldBe mapOf("k1" to "v1")
    }

    @Test
    suspend fun `unwrapList fails on an element of the wrong type`() {
        val reply = RArray(listOf(PlainString("a"), Int64(1)))

        shouldThrow<ResponseParsingException> {
            reply.unwrapList<String>()
        }
    }

    @Test
    suspend fun `unwrapList skips null elements`() {
        val reply = RArray(listOf(PlainString("a"), RType.Null, PlainString("c")))

        reply.unwrapList<String>() shouldBe listOf("a", "c")
    }

    @Test
    suspend fun `verbatim string unwraps to the bare content while value keeps the encoding prefix`() {
        val verbatim = Buffer {
            writeFully("=15\r\ntxt:Some string\r\n".encodeToByteArray())
        }.readResponseWrapped() as VerbatimString

        verbatim.encoding shouldBe "txt"
        verbatim.data shouldBe "Some string"
        verbatim.unwrap<String>() shouldBe "Some string"
        verbatim.value shouldBe "txt:Some string"
        RArray(listOf(verbatim)).unwrapList<String>() shouldBe listOf("Some string")
    }
}
