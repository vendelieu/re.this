package eu.vendeli.rethis.commands.cluster

import eu.vendeli.rethis.TestCtx
import eu.vendeli.rethis.codecs.cluster.ClusterInfoCommandCodec
import eu.vendeli.rethis.codecs.cluster.ClusterNodesCommandCodec
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.io.Buffer

// RESP3 servers answer CLUSTER INFO / CLUSTER NODES with a verbatim string (`=<len>\r\ntxt:<payload>\r\n`).
class ClusterVerbatimReplyTest : TestCtx() {
    private suspend fun verbatim(payload: String): Buffer = Buffer {
        writeFully("=${payload.length + 4}\r\ntxt:$payload\r\n".encodeToByteArray())
    }

    @Test
    suspend fun `CLUSTER INFO decodes a verbatim string reply`() {
        val payload = "cluster_state:ok\r\ncluster_slots_assigned:16384\r\n"
        ClusterInfoCommandCodec.decode(verbatim(payload), Charsets.UTF_8) shouldBe payload
    }

    @Test
    suspend fun `CLUSTER NODES decodes a verbatim string reply`() {
        val payload = "b4fafeaf4236621376603bc52ed5d8ffc6806d4e :6379@16379 myself,master - 0 0 0 connected 0-16383\n"
        ClusterNodesCommandCodec.decode(verbatim(payload), Charsets.UTF_8) shouldBe payload
    }
}
