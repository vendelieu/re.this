package eu.vendeli.rethis.shared.decoders.cluster

import eu.vendeli.rethis.shared.decoders.ResponseDecoder
import eu.vendeli.rethis.shared.request.cluster.SlotRange
import eu.vendeli.rethis.shared.response.cluster.Cluster
import eu.vendeli.rethis.shared.response.cluster.ClusterNode
import eu.vendeli.rethis.shared.response.common.HostAndPort
import eu.vendeli.rethis.shared.types.RespCode
import eu.vendeli.rethis.shared.types.ResponseParsingException
import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import eu.vendeli.rethis.shared.utils.resolveToken
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLineStrict

object ClusterSlotsDecoder : ResponseDecoder<Cluster> {
    private val EMPTY_CLUSTER = Cluster(emptyList())
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        code: RespCode?,
    ): Cluster {
        if (input == EMPTY_BUFFER) return EMPTY_CLUSTER
        // Read top-level array header
        if (code == null) input.resolveToken(RespCode.ARRAY)

        val total = input.readLineStrict().toInt()
        val nodeEntries = mutableListOf<ClusterNode>()

        repeat(total) {
            // each slot entry
            if (input.readByte() != RespCode.ARRAY.code) throw ResponseParsingException(
                "Expected ARRAY token for slot block",
            )
            // count of elements
            val elements = input.readLineStrict().toInt()

            val start = input.readLineStrict().toLong()
            val end = input.readLineStrict().toLong()
            val range = SlotRange(start, end)

            // master info
            val masterHost = input.readLineStrict()
            val masterPort = input.readLineStrict().toInt()
            input.readLineStrict()  // ignored here
            // skip metadata array
            skipMetadata(input)
            val master = HostAndPort(masterHost, masterPort)
            val replicas = mutableListOf<HostAndPort>()

            // replicas
            val replicasCount = elements - 3
            repeat(replicasCount) {
                val host = input.readLineStrict()
                val port = input.readLineStrict().toInt()
                // skip id and metadata
                input.readLineStrict() // id
                skipMetadata(input)
                replicas += HostAndPort(host, port)
            }

            nodeEntries += ClusterNode(master = master, ranges = listOf(range), replicas = replicas)
        }

        // merge by HostAndPort
        val merged = linkedMapOf<HostAndPort, ClusterNode>()
        for (n in nodeEntries) {
            val entry = merged[n.master]
            if (entry != null) {
                merged[n.master] = ClusterNode(
                    entry.master,
                    entry.ranges.toMutableList().apply { addAll(n.ranges) }.toList(),
                    entry.replicas.toMutableList().apply { addAll(n.replicas) }.toList(),
                )
            } else {
                merged[n.master] = n
            }
        }
        return Cluster(merged.values.toList())
    }

    private fun skipMetadata(input: Buffer) {
        // may be nested metadata array, read and discard
        val b = input.readByte()
        if (b == RespCode.ARRAY.code) {
            val count = input.readLineStrict().toInt()
            repeat(count) { input.readLineStrict() }
        } else {
            input.writeByte(b)
        }
    }
}
