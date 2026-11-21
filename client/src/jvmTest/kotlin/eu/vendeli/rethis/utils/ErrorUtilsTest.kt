package eu.vendeli.rethis.utils

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.shared.types.CrossSlotOperationException
import eu.vendeli.rethis.shared.types.DownReadOnlyStateException
import eu.vendeli.rethis.shared.types.DownUnboundSlotException
import eu.vendeli.rethis.shared.types.ReThisException
import eu.vendeli.rethis.shared.types.RedirectAskException
import eu.vendeli.rethis.shared.types.RedirectMovedException
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.utils.tryInferCause
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer
import kotlinx.io.readString

class ErrorUtilsTest : TestCtx() {
    private suspend fun bufferOf(raw: String): Buffer = Buffer {
        writeFully(raw.encodeToByteArray())
    }

    private suspend fun bulkErrorBody(payload: String, trailing: String = ""): Buffer =
        bufferOf("${payload.encodeToByteArray().size}\r\n$payload\r\n$trailing")

    @Test
    suspend fun `bulk error moved redirect is inferred from the payload only`() {
        val cause = bulkErrorBody("MOVED 1 h:6379").tryInferCause(RespCode.BULK_ERROR)

        val moved = cause.shouldBeInstanceOf<RedirectMovedException>()
        moved.slot shouldBe 1
        moved.host shouldBe "h"
        moved.port shouldBe 6379
    }

    @Test
    suspend fun `bulk error crossslot is inferred`() {
        val cause = bulkErrorBody("CROSSSLOT Keys in request don't hash to the same slot")
            .tryInferCause(RespCode.BULK_ERROR)

        cause.shouldBeInstanceOf<CrossSlotOperationException>()
        cause.message shouldBe "CROSSSLOT Keys in request don't hash to the same slot"
    }

    @Test
    suspend fun `error codes are matched as whole tokens`() {
        listOf(
            "ASKED something",
            "MOVEDX 1 h:1",
            "CROSSSLOTS foo",
            "TRYAGAINST x",
            "CLUSTERDOWNX Hash slot not served",
        ).forEach { line ->
            val cause = simpleError(line).shouldBeInstanceOf<ReThisException>()

            cause::class shouldBe ReThisException::class
            cause.message shouldBe line
        }
    }

    @Test
    suspend fun `bulk error redirect with a spaced token is still inferred`() {
        bulkErrorBody("ASK 1 h:6379").tryInferCause(RespCode.BULK_ERROR).shouldBeInstanceOf<RedirectAskException>()
    }

    @Test
    suspend fun `bulk error payload is decoded with the given charset`() {
        val payload = "ERR caf\u00e9"
        val bytes = payload.toByteArray(Charsets.ISO_8859_1)
        val buffer = Buffer().apply {
            writeFully("${bytes.size}\r\n".encodeToByteArray())
            writeFully(bytes)
            writeFully("\r\n".encodeToByteArray())
        }

        buffer.tryInferCause(RespCode.BULK_ERROR, Charsets.ISO_8859_1)?.message shouldBe payload
    }

    @Test
    suspend fun `bulk error with unknown payload keeps the bare payload as message`() {
        val cause = bulkErrorBody("ERR something odd").tryInferCause(RespCode.BULK_ERROR)

        cause.shouldBeInstanceOf<ReThisException>()
        cause.message shouldBe "ERR something odd"
    }

    @Test
    suspend fun `bulk error inference consumes through the payload CRLF only`() {
        val buffer = bulkErrorBody("ERR first", trailing = "+OK\r\n")

        buffer.tryInferCause(RespCode.BULK_ERROR)

        buffer.readString() shouldBe "+OK\r\n"
    }

    private suspend fun simpleError(line: String) = bufferOf("$line\r\n").tryInferCause(RespCode.SIMPLE_ERROR)

    @Test
    suspend fun `moved redirect with ipv6 host splits on the last colon`() {
        val moved = simpleError("MOVED 3999 ::1:6381").shouldBeInstanceOf<RedirectMovedException>()

        moved.slot shouldBe 3999
        moved.host shouldBe "::1"
        moved.port shouldBe 6381
    }

    @Test
    suspend fun `ask redirect with ipv6 host splits on the last colon`() {
        val ask = simpleError("ASK 12 fd00::5:6379").shouldBeInstanceOf<RedirectAskException>()

        ask.slot shouldBe 12
        ask.host shouldBe "fd00::5"
        ask.port shouldBe 6379
    }

    @Test
    suspend fun `moved redirect with ipv4 host keeps the host`() {
        val moved = simpleError("MOVED 3999 127.0.0.1:6381").shouldBeInstanceOf<RedirectMovedException>()

        moved.host shouldBe "127.0.0.1"
        moved.port shouldBe 6381
    }

    @Test
    suspend fun `moved redirect without host keeps an empty host`() {
        val moved = simpleError("MOVED 3999 :6381").shouldBeInstanceOf<RedirectMovedException>()

        moved.host shouldBe ""
        moved.port shouldBe 6381
    }

    @Test
    suspend fun `malformed redirect lines fall back to a plain exception`() {
        listOf("MOVED 5", "MOVED x host:1", "MOVED 5 hostonly", "ASK 5 host:port").forEach { line ->
            val cause = simpleError(line)

            cause.shouldBeInstanceOf<ReThisException>()
            cause::class shouldBe ReThisException::class
            cause.message shouldBe line
        }
    }

    // Strings verified against redis/redis src/cluster.c (clusterRedirectClient) on 7.2 and unstable.
    @Test
    suspend fun `clusterdown hash slot not served maps to unbound slot`() {
        simpleError("CLUSTERDOWN Hash slot not served").shouldBeInstanceOf<DownUnboundSlotException>()
    }

    @Test
    suspend fun `clusterdown read-only state maps to read only state`() {
        simpleError("CLUSTERDOWN The cluster is down and only accepts read commands")
            .shouldBeInstanceOf<DownReadOnlyStateException>()
    }

    @Test
    suspend fun `other clusterdown messages stay a plain exception`() {
        listOf("CLUSTERDOWN foo", "CLUSTERDOWN The cluster is down").forEach { line ->
            val cause = simpleError(line).shouldBeInstanceOf<ReThisException>()

            cause::class shouldBe ReThisException::class
            cause.message shouldBe line
        }
    }
}
