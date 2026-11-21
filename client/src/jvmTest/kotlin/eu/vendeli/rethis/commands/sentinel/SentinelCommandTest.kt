package eu.vendeli.rethis.commands.sentinel

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.commands.support.TopologyFixtures
import eu.vendeli.rethis.codecs.sentinel.SentinelGetMasterAddrCommandCodec
import eu.vendeli.rethis.codecs.sentinel.SentinelResetCommandCodec
import eu.vendeli.rethis.codecs.sentinel.SentinelCkQuorumCommandCodec
import eu.vendeli.rethis.shared.types.ResponseParsingException
import eu.vendeli.rethis.shared.utils.readResponseWrapped
import eu.vendeli.rethis.shared.utils.unwrapList
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

class SentinelCommandTest : TestCtx() {
    @Test
    fun `decode valid payload`() {
        val result = TopologyFixtures
            .validSentinelMasterAddressResponse()
            .readResponseWrapped(Charsets.UTF_8)
            .unwrapList<String>()

        result shouldBe listOf("127.0.0.1", "6379")
    }

    @Test
    fun `decode empty payload`() {
        val result = TopologyFixtures
            .emptyBuffer()
            .readResponseWrapped(Charsets.UTF_8)
            .unwrapList<String>()

        result shouldBe emptyList()
    }

    @Test
    fun `decode malformed payload`() {
        shouldThrow<ResponseParsingException> {
            TopologyFixtures
                .malformedSentinelMasterAddressResponse()
                .readResponseWrapped(Charsets.UTF_8)
                .unwrapList<String>()
        }
    }

    @Test
    fun `decode valid replicas payload`() {
        val result = TopologyFixtures
            .validSentinelReplicasResponse()
            .readResponseWrapped(Charsets.UTF_8)
            .unwrapList<String>()

        result.size shouldBe 2
        result.all { it.contains("slave") } shouldBe true
    }

    @Test
    fun `decode malformed replicas payload`() {
        shouldThrow<ResponseParsingException> {
            TopologyFixtures
                .malformedSentinelReplicasResponse()
                .readResponseWrapped(Charsets.UTF_8)
                .unwrapList<String>()
        }
    }

    @Test
    fun `CKQUORUM success message decodes to true`() {
        val reply = "+OK 1 usable Sentinels. Quorum and failover authorization can be reached\r\n"

        SentinelCkQuorumCommandCodec.decode(TopologyFixtures.respBuffer(reply), Charsets.UTF_8) shouldBe true
    }

    @Test
    fun `RESET reply counts the masters reset`() {
        SentinelResetCommandCodec.decode(TopologyFixtures.respBuffer(":1\r\n"), Charsets.UTF_8) shouldBe true
        SentinelResetCommandCodec.decode(TopologyFixtures.respBuffer(":0\r\n"), Charsets.UTF_8) shouldBe false
    }

    @Test
    fun `GET-MASTER-ADDR-BY-NAME decodes the host and port of a known master`() {
        val reply = "*2\r\n$9\r\n127.0.0.1\r\n$4\r\n6379\r\n"

        SentinelGetMasterAddrCommandCodec.decode(TopologyFixtures.respBuffer(reply), Charsets.UTF_8) shouldBe
            listOf("127.0.0.1", "6379")
    }

    @Test
    fun `GET-MASTER-ADDR-BY-NAME decodes a null array for an unknown master to null`() {
        // RESP2 null array and RESP3 null, as sent by a sentinel for an unknown master name
        SentinelGetMasterAddrCommandCodec.decode(TopologyFixtures.respBuffer("*-1\r\n"), Charsets.UTF_8) shouldBe null
        SentinelGetMasterAddrCommandCodec.decode(TopologyFixtures.respBuffer("_\r\n"), Charsets.UTF_8) shouldBe null
    }

    @Test
    fun `GET-MASTER-ADDR-BY-NAME rejects a reply that is neither an array nor null`() {
        shouldThrow<ResponseParsingException> {
            SentinelGetMasterAddrCommandCodec.decode(TopologyFixtures.respBuffer(":1\r\n"), Charsets.UTF_8)
        }
    }

    @Test
    fun `RESET reply for a pattern matching no master is false`() {
        // bytes captured from a real sentinel: `SENTINEL RESET nomatch*` replies `:0` on RESP2 and RESP3
        SentinelResetCommandCodec.decode(TopologyFixtures.respBuffer(":0\r\n"), Charsets.UTF_8) shouldBe false
    }

    @Test
    fun `CKQUORUM reply that is not OK decodes to false`() {
        val reply = "+NOQUORUM 1 usable Sentinels. Not enough available Sentinels to reach the majority\r\n"

        SentinelCkQuorumCommandCodec.decode(TopologyFixtures.respBuffer(reply), Charsets.UTF_8) shouldBe false
    }
}
