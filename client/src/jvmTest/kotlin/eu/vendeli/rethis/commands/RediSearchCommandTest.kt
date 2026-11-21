package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.command.search.ftCreate
import eu.vendeli.rethis.command.search.ftDictAdd
import eu.vendeli.rethis.command.search.ftDictDel
import eu.vendeli.rethis.command.search.ftDictDump
import eu.vendeli.rethis.command.search.ftDropIndex
import eu.vendeli.rethis.command.search.ftList
import eu.vendeli.rethis.command.search.ftSugAdd
import eu.vendeli.rethis.command.search.ftSugLen
import eu.vendeli.rethis.command.search.ftTagVals
import eu.vendeli.rethis.command.hash.hSet
import eu.vendeli.rethis.shared.request.common.FieldValue
import eu.vendeli.rethis.types.common.RespVer
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds
import eu.vendeli.rethis.shared.types.PlainString
import eu.vendeli.rethis.shared.types.RSet
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf

class RediSearchCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test FT_CREATE and FT__LIST commands`() {
        client.ftCreate("idx-1", "ON", "HASH", "PREFIX", "1", "doc:", "SCHEMA", "title", "TEXT") shouldBe "OK"
        client.ftList().shouldBeTypeOf<RSet>().value shouldContain PlainString("idx-1")
        client.ftDropIndex("idx-1", null) shouldBe "OK"
        client.ftList().shouldBeTypeOf<RSet>().value shouldNotContain PlainString("idx-1")
    }

    @Test
    suspend fun `test FT_SUGADD and FT_SUGLEN commands`() {
        client.ftSugAdd("sugKey1", "hello", 1.0, null, null) shouldBe 1L
        client.ftSugAdd("sugKey1", "world", 1.0, null, null) shouldBe 2L
        client.ftSugLen("sugKey1") shouldBe 2L
    }

    @Test
    suspend fun `test FT_TAGVALS returns the distinct tag values on RESP2 and RESP3`() {
        listOf(RespVer.V2, RespVer.V3).forEach { protocol ->
            val c = createClient(protocol = protocol)
            val index = "tagIdx-$protocol"
            try {
                withTimeout(30.seconds) {
                    c.ftCreate(index, "ON", "HASH", "PREFIX", "1", "tag$protocol:", "SCHEMA", "color", "TAG")
                    c.hSet("tag$protocol:1", FieldValue("color", "red"))
                    c.hSet("tag$protocol:2", FieldValue("color", "blue"))
                    c.ftTagVals(index, "color") shouldBe setOf("red", "blue")
                }
            } finally {
                runCatching { withTimeout(5.seconds) { c.ftDropIndex(index, null) } }
                c.close()
            }
        }
    }

    @Test
    suspend fun `test FT_DICTDUMP returns the dictionary terms on RESP2 and RESP3`() {
        listOf(RespVer.V2, RespVer.V3).forEach { protocol ->
            val c = createClient(protocol = protocol)
            val dict = "dict-$protocol"
            try {
                withTimeout(30.seconds) {
                    c.ftDictAdd(dict, "alpha", "beta") shouldBe 2L
                    c.ftDictDump(dict) shouldBe setOf("alpha", "beta")
                }
            } finally {
                runCatching { withTimeout(5.seconds) { c.ftDictDel(dict, "alpha", "beta") } }
                c.close()
            }
        }
    }
}
