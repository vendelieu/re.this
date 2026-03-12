package eu.vendeli.rethis.commands.cluster

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.commands.support.TopologyFixtures
import eu.vendeli.rethis.shared.decoders.cluster.ClusterSlotsDecoder
import eu.vendeli.rethis.shared.types.RedirectAskException
import eu.vendeli.rethis.shared.types.RedirectMovedException
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.ResponseParsingException
import eu.vendeli.rethis.shared.utils.tryInferCause
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.core.writeFully
import kotlinx.io.Buffer

class ClusterCommandTest : TestCtx() {
    @Test
    suspend fun `decode valid payload`() {
        val result = ClusterSlotsDecoder.decode(TopologyFixtures.validClusterSlotsResponse(), Charsets.UTF_8, null)

        result.nodes.size shouldBe 1
        result.nodes.first().master.host shouldBe "127.0.0.1"
        result.nodes.first().master.port shouldBe 7000
        result.nodes.first().replicas.size shouldBe 1
        result.nodes.first().ranges.single().start shouldBe 0
        result.nodes.first().ranges.single().end shouldBe 16383
    }

    @Test
    suspend fun `decode empty payload`() {
        val result = ClusterSlotsDecoder.decode(TopologyFixtures.emptyBuffer(), Charsets.UTF_8, null)
        result.nodes shouldBe emptyList()
    }

    @Test
    suspend fun `decode malformed payload`() {
        shouldThrow<ResponseParsingException> {
            ClusterSlotsDecoder.decode(TopologyFixtures.malformedClusterSlotsResponse(), Charsets.UTF_8, null)
        }
    }

    @Test
    suspend fun `infer MOVED redirect error`() {
        val buffer = Buffer().apply {
            writeFully("MOVED 42 127.0.0.1:7002\r\n".encodeToByteArray())
        }

        val exception = buffer.tryInferCause(RespCode.SIMPLE_ERROR)
        (exception is RedirectMovedException) shouldBe true
        (exception as RedirectMovedException).slot shouldBe 42
        exception.host shouldBe "127.0.0.1"
        exception.port shouldBe 7002
    }

    @Test
    suspend fun `infer ASK redirect error`() {
        val buffer = Buffer().apply {
            writeFully("ASK 128 127.0.0.1:7003\r\n".encodeToByteArray())
        }

        val exception = buffer.tryInferCause(RespCode.SIMPLE_ERROR)
        (exception is RedirectAskException) shouldBe true
        (exception as RedirectAskException).slot shouldBe 128
        exception.host shouldBe "127.0.0.1"
        exception.port shouldBe 7003
    }
}
