package eu.vendeli.rethis.api.spec.common.decoders.cluster

import eu.vendeli.rethis.api.spec.common.decoders.ResponseDecoder
import eu.vendeli.rethis.api.spec.common.request.cluster.SlotRange
import eu.vendeli.rethis.api.spec.common.response.common.HostAndPort
import eu.vendeli.rethis.api.spec.common.response.cluster.Cluster
import eu.vendeli.rethis.api.spec.common.response.cluster.ClusterNode
import eu.vendeli.rethis.api.spec.common.types.RespCode
import eu.vendeli.rethis.api.spec.common.types.ResponseParsingException
import eu.vendeli.rethis.api.spec.common.utils.tryInferCause
import io.ktor.utils.io.charsets.*
import kotlinx.io.Buffer
import kotlinx.io.readLineStrict

object ClusterSlotsDecoder : ResponseDecoder<Cluster> {
    override suspend fun decode(
        input: Buffer,
        charset: Charset,
        withCode: Boolean,
    ): Cluster {
        // Read top-level array header
        val code = RespCode.fromCode(input.readByte())
        if (code != RespCode.ARRAY) throw ResponseParsingException(
            "Expected ARRAY token for CLUSTER SLOTS response", input.tryInferCause(code),
        )
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
            val masterId = input.readLineStrict()  // ignored here
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
