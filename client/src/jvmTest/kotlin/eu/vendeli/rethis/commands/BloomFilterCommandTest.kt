package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.cast
import eu.vendeli.rethis.codecs.bf.BfLoadChunkBACommandCodec
import eu.vendeli.rethis.command.bf.bfAdd
import eu.vendeli.rethis.command.bf.bfCard
import eu.vendeli.rethis.command.bf.bfExists
import eu.vendeli.rethis.command.bf.bfMAdd
import eu.vendeli.rethis.command.bf.bfMExists
import eu.vendeli.rethis.command.bf.bfLoadChunkBA
import eu.vendeli.rethis.command.bf.bfReserve
import eu.vendeli.rethis.command.bf.bfScandump
import eu.vendeli.rethis.shared.types.Bool
import eu.vendeli.rethis.shared.types.BulkString
import eu.vendeli.rethis.shared.types.Int64
import eu.vendeli.rethis.shared.types.RArray
import io.kotest.matchers.shouldBe
import kotlinx.io.readByteArray

class BloomFilterCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test BF_RESERVE and BF_ADD command`() {
        client.bfReserve("bfKey1", 0.01, 1000L, null, null) shouldBe "OK"
        client.bfAdd("bfKey1", "hello") shouldBe true
        client.bfExists("bfKey1", "hello") shouldBe true
        client.bfExists("bfKey1", "missing") shouldBe false
    }

    @Test
    suspend fun `test BF_CARD command`() {
        client.bfReserve("bfKey2", 0.01, 100L, null, null) shouldBe "OK"
        client.bfAdd("bfKey2", "a")
        client.bfAdd("bfKey2", "b")
        client.bfCard("bfKey2") shouldBe 2L
    }

    @Test
    suspend fun `test BF_MADD and BF_MEXISTS commands`() {
        client.bfReserve("bfKey3", 0.01, 100L, null, null) shouldBe "OK"
        client.bfMAdd("bfKey3", "x", "y", "z") shouldBe RArray(listOf(Bool(true), Bool(true), Bool(true)))
        client.bfMExists("bfKey3", "x", "missing") shouldBe RArray(listOf(Bool(true), Bool(false)))
    }

    @Test
    suspend fun `test BF_SCANDUMP and BF_LOADCHUNK round trip through ByteArray chunks`() {
        client.bfReserve("bfDump1", 0.01, 100L, null, null) shouldBe "OK"
        client.bfAdd("bfDump1", "hello") shouldBe true

        val chunks = mutableListOf<Pair<Long, ByteArray>>()
        var iterator = 0L
        while (true) {
            val reply = client.bfScandump("bfDump1", iterator).cast<RArray>().value
            iterator = reply[0].cast<Int64>().value
            if (iterator == 0L) break
            chunks += iterator to reply[1].cast<BulkString>().value.readByteArray()
        }

        // at least one chunk is not valid UTF-8, so a String payload cannot carry it
        chunks.any { (_, bytes) -> !bytes.contentEquals(bytes.decodeToString().encodeToByteArray()) } shouldBe true

        chunks.forEach { (iter, bytes) -> client.bfLoadChunkBA("bfDump2", iter, bytes) shouldBe "OK" }
        client.bfExists("bfDump2", "hello") shouldBe true
        client.bfExists("bfDump2", "missing") shouldBe false
    }

    @Test
    suspend fun `test BF_LOADCHUNK BA codec writes the raw bytes unchanged`() {
        val payload = byteArrayOf(0x00, 0x7F, 0x80.toByte(), 0xFF.toByte())
        val bytes = BfLoadChunkBACommandCodec.encode(defaultCharset, "k", 7L, payload).data.readByteArray()
        val tail = "\r\n\$4\r\n".encodeToByteArray() + payload + "\r\n".encodeToByteArray()
        bytes.takeLast(tail.size) shouldBe tail.toList()
    }
}
