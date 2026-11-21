package eu.vendeli.rethis.utils

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.shared.utils.readCompleteResponseInto
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer

class RespFramingNestedAttributeTest : TestCtx() {
    private suspend fun frame(payload: String, attributes: Buffer? = null): Pair<String, String> {
        val target = Buffer()
        val channel = ByteReadChannel(payload.encodeToByteArray())
        channel.readCompleteResponseInto(target, attributes)
        return target.readText() to channel.readRemaining().readText()
    }

    @Test
    suspend fun `attribute before a nested element does not count as an element`() {
        val frame = "*2\r\n|1\r\n+k\r\n+v\r\n:1\r\n:2\r\n"

        frame("$frame+next\r\n") shouldBe (frame to "+next\r\n")
    }

    @Test
    suspend fun `empty nested attribute does not count as an element`() {
        val frame = "*2\r\n|0\r\n:1\r\n:2\r\n"

        frame("$frame+next\r\n") shouldBe (frame to "+next\r\n")
    }

    @Test
    suspend fun `leading attribute is routed to attributes buffer`() {
        val attributes = Buffer()

        frame("|1\r\n+k\r\n+v\r\n:1\r\n+next\r\n", attributes) shouldBe (":1\r\n" to "+next\r\n")
        attributes.readText() shouldBe "|1\r\n+k\r\n+v\r\n"
    }

    @Test
    suspend fun `stacked leading attributes are all routed to attributes buffer`() {
        val attributes = Buffer()
        val target = Buffer()
        val channel = ByteReadChannel("|1\r\n+k\r\n+v\r\n|1\r\n+k2\r\n+v2\r\n+OK\r\n+NEXT\r\n".encodeToByteArray())

        channel.readCompleteResponseInto(target, attributes)

        target.readText() shouldBe "+OK\r\n"
        attributes.readText() shouldBe "|1\r\n+k\r\n+v\r\n|1\r\n+k2\r\n+v2\r\n"

        val next = Buffer()
        channel.readCompleteResponseInto(next)
        next.readText() shouldBe "+NEXT\r\n"
    }
}
