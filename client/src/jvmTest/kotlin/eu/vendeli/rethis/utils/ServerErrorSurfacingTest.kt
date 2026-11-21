package eu.vendeli.rethis.utils

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.codecs.connection.PingCommandCodec
import eu.vendeli.rethis.codecs.scripting.FunctionKillCommandCodec
import eu.vendeli.rethis.codecs.scripting.ScriptKillCommandCodec
import eu.vendeli.rethis.shared.decoders.generic.WaitAofDecoder
import eu.vendeli.rethis.shared.types.ReThisException
import eu.vendeli.rethis.shared.types.RedirectMovedException
import eu.vendeli.rethis.shared.types.ResponseParsingException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldNotBeInstanceOf
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer

class ServerErrorSurfacingTest : TestCtx() {
    private val charset = Charsets.UTF_8
    private val notBusy = "NOTBUSY No scripts in execution right now."
    private val busy = "BUSY Redis is busy running a script. You can only call SCRIPT KILL or SHUTDOWN NOSAVE."
    private val waitAof = "ERR WAITAOF cannot be used when numlocal is set but appendonly is disabled."

    private suspend fun bufferOf(raw: String): Buffer = Buffer {
        writeFully(raw.encodeToByteArray())
    }

    private fun bulkError(text: String) = "!${text.encodeToByteArray().size}\r\n$text\r\n"

    private fun ReThisException.assertServerText(text: String) {
        message shouldBe text
        shouldNotBeInstanceOf<ResponseParsingException>()
    }

    @Test
    suspend fun `WAITAOF decoder surfaces the server error text`() {
        shouldThrow<ReThisException> { WaitAofDecoder.decode(bufferOf("-$waitAof\r\n"), charset) }
            .assertServerText(waitAof)
    }

    @Test
    suspend fun `WAITAOF decoder surfaces a RESP3 bulk error`() {
        shouldThrow<ReThisException> { WaitAofDecoder.decode(bufferOf(bulkError(waitAof)), charset) }
            .assertServerText(waitAof)
    }

    @Test
    suspend fun `non-error mismatch still reports a parsing failure`() {
        shouldThrow<ResponseParsingException> { WaitAofDecoder.decode(bufferOf(":1\r\n"), charset) }
    }

    @Test
    suspend fun `script kill codec surfaces the server error text`() {
        shouldThrow<ReThisException> { ScriptKillCommandCodec.decode(bufferOf("-$notBusy\r\n"), charset) }
            .assertServerText(notBusy)
    }

    @Test
    suspend fun `function kill codec surfaces the server error text`() {
        shouldThrow<ReThisException> { FunctionKillCommandCodec.decode(bufferOf("-$notBusy\r\n"), charset) }
            .assertServerText(notBusy)
    }

    @Test
    suspend fun `codec surfaces a RESP3 bulk error`() {
        shouldThrow<ReThisException> { ScriptKillCommandCodec.decode(bufferOf(bulkError(notBusy)), charset) }
            .assertServerText(notBusy)
    }

    @Test
    suspend fun `ping codec surfaces the busy error text`() {
        shouldThrow<ReThisException> { PingCommandCodec.decode(bufferOf("-$busy\r\n"), charset) }
            .assertServerText(busy)
    }

    @Test
    suspend fun `codec surfaces a typed redirect`() {
        shouldThrow<ReThisException> {
            ScriptKillCommandCodec.decode(bufferOf("-MOVED 3999 127.0.0.1:6381\r\n"), charset)
        }.shouldBeInstanceOf<RedirectMovedException>()
    }
}
