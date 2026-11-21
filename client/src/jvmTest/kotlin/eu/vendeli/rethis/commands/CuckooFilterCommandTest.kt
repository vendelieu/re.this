package eu.vendeli.rethis.commands

import eu.vendeli.rethis.ReThisTestCtx
import eu.vendeli.rethis.cast
import eu.vendeli.rethis.codecs.cf.CfLoadChunkBACommandCodec
import eu.vendeli.rethis.command.cf.cfAdd
import eu.vendeli.rethis.command.cf.cfCount
import eu.vendeli.rethis.command.cf.cfDel
import eu.vendeli.rethis.command.cf.cfExists
import eu.vendeli.rethis.command.cf.cfLoadChunkBA
import eu.vendeli.rethis.command.cf.cfScandump
import eu.vendeli.rethis.command.cf.cfMExists
import eu.vendeli.rethis.command.cf.cfReserve
import eu.vendeli.rethis.shared.types.Bool
import eu.vendeli.rethis.shared.types.BulkString
import eu.vendeli.rethis.shared.types.Int64
import eu.vendeli.rethis.shared.types.RArray
import io.kotest.matchers.shouldBe
import kotlinx.io.readByteArray

class CuckooFilterCommandTest : ReThisTestCtx() {
    @Test
    suspend fun `test CF_RESERVE and CF_ADD command`() {
        client.cfReserve("cfKey1", 100L, null, null, null) shouldBe "OK"
        client.cfAdd("cfKey1", "hello") shouldBe true
        client.cfExists("cfKey1", "hello") shouldBe true
        client.cfExists("cfKey1", "missing") shouldBe false
    }

    @Test
    suspend fun `test CF_COUNT command`() {
        client.cfReserve("cfKey2", 100L, null, null, null) shouldBe "OK"
        client.cfAdd("cfKey2", "item")
        client.cfAdd("cfKey2", "item")
        client.cfCount("cfKey2", "item") shouldBe 2L
    }

    @Test
    suspend fun `test CF_DEL and CF_MEXISTS commands`() {
        client.cfReserve("cfKey3", 100L, null, null, null) shouldBe "OK"
        client.cfAdd("cfKey3", "a")
        client.cfMExists("cfKey3", "a", "b") shouldBe RArray(listOf(Bool(true), Bool(false)))
        client.cfDel("cfKey3", "a") shouldBe true
    }

    @Test
    suspend fun `test CF_SCANDUMP and CF_LOADCHUNK round trip through ByteArray chunks`() {
        client.cfReserve("cfDump1", 100L, null, null, null) shouldBe "OK"
        client.cfAdd("cfDump1", "hello") shouldBe true

        val chunks = mutableListOf<Pair<Long, ByteArray>>()
        var iterator = 0L
        while (true) {
            val reply = client.cfScandump("cfDump1", iterator).cast<RArray>().value
            iterator = reply[0].cast<Int64>().value
            if (iterator == 0L) break
            chunks += iterator to reply[1].cast<BulkString>().value.readByteArray()
        }

        // at least one chunk is not valid UTF-8, so a String payload cannot carry it
        chunks.any { (_, bytes) -> !bytes.contentEquals(bytes.decodeToString().encodeToByteArray()) } shouldBe true

        chunks.forEach { (iter, bytes) -> client.cfLoadChunkBA("cfDump2", iter, bytes) shouldBe "OK" }
        client.cfExists("cfDump2", "hello") shouldBe true
        client.cfExists("cfDump2", "missing") shouldBe false
    }

    @Test
    suspend fun `test CF_LOADCHUNK BA codec writes the raw bytes unchanged`() {
        val payload = byteArrayOf(0x00, 0x7F, 0x80.toByte(), 0xFF.toByte())
        val bytes = CfLoadChunkBACommandCodec.encode(defaultCharset, "k", 7L, payload).data.readByteArray()
        val tail = "\r\n\$4\r\n".encodeToByteArray() + payload + "\r\n".encodeToByteArray()
        bytes.takeLast(tail.size) shouldBe tail.toList()
    }
}
