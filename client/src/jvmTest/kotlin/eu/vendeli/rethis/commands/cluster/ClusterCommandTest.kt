package eu.vendeli.rethis.commands.cluster

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.commands.support.TopologyFixtures
import eu.vendeli.rethis.shared.decoders.cluster.ClusterShardsDecoder
import eu.vendeli.rethis.shared.decoders.cluster.ClusterSlotsDecoder
import eu.vendeli.rethis.shared.response.cluster.ShardNode
import eu.vendeli.rethis.shared.response.common.HostAndPort
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
        result.nodes
            .first()
            .master.host shouldBe "127.0.0.1"
        result.nodes
            .first()
            .master.port shouldBe 7000
        result.nodes
            .first()
            .replicas.size shouldBe 1
        result.nodes
            .first()
            .ranges
            .single()
            .start shouldBe 0
        result.nodes
            .first()
            .ranges
            .single()
            .end shouldBe 16383
    }

    @Test
    suspend fun `decode resp3 payload with metadata map and merged ranges`() {
        val result = ClusterSlotsDecoder.decode(TopologyFixtures.resp3ClusterSlotsResponse(), Charsets.UTF_8, null)

        result.nodes.size shouldBe 1
        val node = result.nodes.single()
        node.master shouldBe HostAndPort("127.0.0.1", 7000)
        node.ranges.map { it.start to it.end } shouldBe listOf(0L to 8191L, 8192L to 16383L)
        node.replicas shouldBe emptyList()
    }

    @Test
    suspend fun `decode resp2 cluster shards payload`() {
        val shards = ClusterShardsDecoder.decode(TopologyFixtures.resp2ClusterShardsResponse(), Charsets.UTF_8, null)

        shards.size shouldBe 1
        val shard = shards.single()
        shard.slots.single().let { it.start to it.end } shouldBe (0L to 16383L)
        shard.nodes.size shouldBe 2

        val master = shard.nodes.first()
        master.id shouldBe "shard-node-1"
        master.port shouldBe 7000
        master.ip shouldBe "127.0.0.1"
        master.role shouldBe "master"
        master.replicationOffset shouldBe 72156L
        master.health shouldBe ShardNode.HealthStatus.ONLINE

        shard.nodes.last().role shouldBe "replica"
    }

    @Test
    suspend fun `decode resp3 cluster shards payload`() {
        val shards = ClusterShardsDecoder.decode(TopologyFixtures.resp3ClusterShardsResponse(), Charsets.UTF_8, null)

        val node = shards.single().nodes.single()
        node.id shouldBe "shard-node-1"
        node.port shouldBe 6379
        node.tlsPort shouldBe 7443
        node.hostname shouldBe null
        node.role shouldBe "replica"
        node.health shouldBe ShardNode.HealthStatus.LOADING
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
