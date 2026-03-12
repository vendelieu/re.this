package eu.vendeli.rethis.commands.sentinel

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.commands.support.TopologyFixtures
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
        shouldThrow<NumberFormatException> {
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
}
