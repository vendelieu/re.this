package eu.vendeli.rethis.shared.decoders.cluster

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.request.cluster.SlotRange
import eu.vendeli.rethis.shared.response.cluster.Cluster
import eu.vendeli.rethis.shared.response.cluster.ClusterNode
import eu.vendeli.rethis.shared.response.common.HostAndPort
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.ResponseParsingException
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.readResponseWrapped
import eu.vendeli.rethis.shared.utils.unwrap
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer

object ClusterSlotsDecoder : ResponseDecoder<Cluster> {
    private val EMPTY_CLUSTER = Cluster(emptyList())
    private const val RANGE_FIELDS = 2 // start and end precede the node entries in a slot block

    override fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): Cluster {
        if (input == EMPTY_BUFFER) return EMPTY_CLUSTER
        val reply = input.readResponseWrapped(charset, code = code)
        if (reply is RType.Null) return EMPTY_CLUSTER

        val nodeEntries = reply.asArray("CLUSTER SLOTS reply").map { entry ->
            val fields = entry.asArray("slot block")
            if (fields.size <= RANGE_FIELDS) throw ResponseParsingException(
                "Invalid slot block: expected start, end and at least a master node, got ${fields.size} elements",
            )

            val range = SlotRange(fields[0].asLong("slot range start"), fields[1].asLong("slot range end"))
            val nodes = fields.drop(RANGE_FIELDS).map { it.toHostAndPort() }

            ClusterNode(master = nodes.first(), ranges = listOf(range), replicas = nodes.drop(1))
        }

        // merge slot ranges and replicas of blocks served by the same master
        val merged = linkedMapOf<HostAndPort, ClusterNode>()
        for (node in nodeEntries) {
            val entry = merged[node.master]
            merged[node.master] = if (entry == null) node else ClusterNode(
                entry.master,
                entry.ranges + node.ranges,
                entry.replicas + node.replicas,
            )
        }
        return Cluster(merged.values.toList())
    }

    // node description: [host, port, node-id, metadata] — id and metadata (array in RESP2, map in RESP3) are ignored
    private fun RType.toHostAndPort(): HostAndPort {
        val node = asArray("node description")
        if (node.size < 2) throw ResponseParsingException(
            "Invalid node description: expected at least host and port, got ${node.size} elements",
        )
        val host = node[0].unwrap<String>() ?: throw ResponseParsingException(
            "Invalid node host: ${node[0]}",
        )
        return HostAndPort(host, node[1].asLong("node port").toInt())
    }

}
