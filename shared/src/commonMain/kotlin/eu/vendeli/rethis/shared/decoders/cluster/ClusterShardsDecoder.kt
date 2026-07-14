package eu.vendeli.rethis.shared.decoders.cluster

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.request.cluster.SlotRange
import eu.vendeli.rethis.shared.response.cluster.Shard
import eu.vendeli.rethis.shared.response.cluster.ShardNode
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.ResponseParsingException
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.readResponseWrapped
import eu.vendeli.rethis.shared.utils.unwrap
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer

object ClusterShardsDecoder : ResponseDecoder<List<Shard>> {
    override fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): List<Shard> {
        if (input == EMPTY_BUFFER) return emptyList()
        val reply = input.readResponseWrapped(charset, code = code)
        if (reply is RType.Null) return emptyList()

        return reply.asArray("CLUSTER SHARDS reply").map { entry ->
            val fields = entry.asFields("shard block")

            // slots come as a flat array of start-end integer pairs
            val slotBounds = fields["slots"]?.asArray("shard slots")?.map { it.asLong("slot bound") }
                ?: throw ResponseParsingException("Missing 'slots' field in shard block")
            if (slotBounds.size % 2 != 0) throw ResponseParsingException(
                "Invalid shard slots: expected start-end pairs, got ${slotBounds.size} bounds",
            )

            val nodes = fields["nodes"]?.asArray("shard nodes")?.map { it.toShardNode() }
                ?: throw ResponseParsingException("Missing 'nodes' field in shard block")

            Shard(
                slots = slotBounds.chunked(2).map { SlotRange(it[0], it[1]) },
                nodes = nodes,
            )
        }
    }

    private fun RType.toShardNode(): ShardNode {
        val fields = asFields("node description")

        fun text(key: String): String? = fields[key]?.unwrap<String>()
        fun number(key: String): Long? = fields[key]?.takeUnless { it is RType.Null }?.asLong(key)

        val health = text("health") ?: throw ResponseParsingException("Missing 'health' in node description")
        return ShardNode(
            id = text("id") ?: throw ResponseParsingException("Missing 'id' in node description"),
            endpoint = text("endpoint")?.takeUnless { it.isBlank() || it == "NULL" },
            ip = text("ip")?.takeUnless { it.isBlank() },
            hostname = text("hostname")?.takeUnless { it.isBlank() },
            port = number("port")?.toInt(),
            tlsPort = number("tls-port")?.toInt(),
            role = text("role") ?: throw ResponseParsingException("Missing 'role' in node description"),
            replicationOffset = number("replication-offset") ?: throw ResponseParsingException(
                "Missing 'replication-offset' in node description",
            ),
            health = ShardNode.HealthStatus.entries.firstOrNull { it.name.equals(health, ignoreCase = true) }
                ?: throw ResponseParsingException("Unknown 'health' value: $health"),
        )
    }
}
