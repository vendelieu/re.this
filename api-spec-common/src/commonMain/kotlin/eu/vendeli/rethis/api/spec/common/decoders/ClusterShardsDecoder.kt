package eu.vendeli.rethis.api.spec.common.decoders

import eu.vendeli.rethis.api.spec.common.request.cluster.SlotRange
import eu.vendeli.rethis.api.spec.common.response.cluster.Shard
import eu.vendeli.rethis.api.spec.common.response.cluster.ShardNode
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.ResponseParsingException
import io.ktor.util.reflect.*
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLine
import kotlinx.io.readLineStrict

object ClusterShardsDecoder : ResponseDecoder<List<Shard>>() {
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        typeInfo: TypeInfo,
    ): List<Shard> {
        val code = input.readByte()
        if (code != RespCode.ARRAY.code) throw ResponseParsingException(
            "Invalid response structure, expected array token, given $code",
        )

        val size = input.readLineStrict().toInt()
        val shards = mutableListOf<Shard>()

        repeat(size) {
            // Each entry may be RESP2 array or RESP3 map
            val nestedType = input.readByte()
            if (nestedType != RespCode.ARRAY.code && nestedType != RespCode.MAP.code) {
                throw ResponseParsingException(
                    "Invalid response structure, expected ARRAY or MAP token for shard block, given ${
                        nestedType.toInt().toChar()
                    }",
                )
            }
            // Delegate to unified unwrapper
            shards += unwrapShard(input)
        }

        return shards
    }

    /**
     * Handles both RESP2 array-of-fields and RESP3 map-of-fields; expects container size next.
     * Reads exactly one Shard: slots and nodes.
     */
    private fun unwrapShard(input: Buffer): Shard {
        // Read and ignore the count of inner elements
        input.readLine()

        // ---- Slots ----
        val key1 = input.readLineStrict()
        if (key1 != "slots") {
            throw ResponseParsingException(
                "Invalid response structure, expected 'slots' field, found '$key1'",
            )
        }
        val slotsSize = input.readLineStrict().toInt()
        val slots = mutableListOf<SlotRange>()
        repeat(slotsSize) {
            val start = input.readLineStrict().toLong()
            val end = input.readLineStrict().toLong()
            slots.add(SlotRange(start, end))
        }

        // ---- Nodes ----
        val key2 = input.readLineStrict()
        if (key2 != "nodes") {
            throw ResponseParsingException(
                "Invalid response structure, expected 'nodes' field, found '$key2'",
            )
        }
        // Next line is number of nodes
        val nodesSize = input.readLineStrict().toInt()
        val nodes = mutableListOf<ShardNode>()

        repeat(nodesSize) {
            var id: String? = null
            var endpoint: String? = null
            var ip: String? = null
            var hostname: String? = null
            var port: Int? = null
            var tlsPort: Int? = null
            var role: String? = null
            var replicationOffset: Long? = null
            var health: String? = null

            while (true) {
                val key = input.readLineStrict()
                if (key.isEmpty()) break
                val value = input.readLineStrict()

                when (key) {
                    "id" -> id = value
                    "endpoint" -> endpoint = if (value == "NULL" || value.isBlank()) null else value
                    "ip" -> ip = value.ifBlank { null }
                    "hostname" -> hostname = value
                    "port" -> port = value.toInt()
                    "tls-port" -> tlsPort = value.toInt()
                    "role" -> role = value
                    "replication-offset" -> replicationOffset = value.toLong()
                    "health" -> health = value
                }

                val type = input.readByte()
                if (type == RespCode.ARRAY.code || type == RespCode.MAP.code) break // End
            }

            nodes.add(
                ShardNode(
                    id = id!!,
                    endpoint = endpoint,
                    ip = ip!!,
                    hostname = hostname,
                    port = port,
                    tlsPort = tlsPort,
                    role = role!!,
                    replicationOffset = replicationOffset!!,
                    health = ShardNode.HealthStatus.valueOf(health!!),
                ),
            )
        }

        return Shard(slots, nodes)
    }
}
