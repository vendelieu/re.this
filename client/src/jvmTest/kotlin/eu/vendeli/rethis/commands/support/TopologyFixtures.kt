package eu.vendeli.rethis.commands.support

import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import io.ktor.utils.io.core.writeFully
import kotlinx.io.Buffer

internal object TopologyFixtures {
    fun respBuffer(payload: String): Buffer = Buffer().apply {
        writeFully(payload.encodeToByteArray())
    }

    fun emptyBuffer(): Buffer = EMPTY_BUFFER

    // real CLUSTER SLOTS wire format: [start, end, master-node, replica-node...],
    // each node an array of [host, port, node-id, metadata] (RESP2: metadata array)
    fun validClusterSlotsResponse(): Buffer = respBuffer(
        """
        *1
        *4
        :0
        :16383
        *4
        $9
        127.0.0.1
        :7000
        $9
        master-id
        *0
        *4
        $9
        127.0.0.1
        :7001
        ${'$'}10
        replica-id
        *0

        """.trimIndent().replace("\n", "\r\n"),
    )

    // RESP3 variant: metadata is a map; two slot blocks served by the same master must merge
    fun resp3ClusterSlotsResponse(): Buffer = respBuffer(
        """
        *2
        *3
        :0
        :8191
        *4
        $9
        127.0.0.1
        :7000
        $9
        master-id
        %1
        $8
        hostname
        ${'$'}10
        my-node.eu
        *3
        :8192
        :16383
        *4
        $9
        127.0.0.1
        :7000
        $9
        master-id
        %1
        $8
        hostname
        ${'$'}10
        my-node.eu

        """.trimIndent().replace("\n", "\r\n"),
    )

    fun malformedClusterSlotsResponse(): Buffer = respBuffer(
        """
        *1
        +4
        
        """.trimIndent().replace("\n", "\r\n"),
    )

    // real CLUSTER SHARDS wire format, RESP2: shard as flat key-value array,
    // slots as flat array of start-end integer pairs, nodes as key-value arrays
    fun resp2ClusterShardsResponse(): Buffer = respBuffer(
        """
        *1
        *4
        $5
        slots
        *2
        :0
        :16383
        $5
        nodes
        *2
        *14
        $2
        id
        $12
        shard-node-1
        $4
        port
        :7000
        $2
        ip
        $9
        127.0.0.1
        $8
        endpoint
        $9
        127.0.0.1
        $4
        role
        $6
        master
        $18
        replication-offset
        :72156
        $6
        health
        $6
        online
        *14
        $2
        id
        $12
        shard-node-2
        $4
        port
        :7001
        $2
        ip
        $9
        127.0.0.1
        $8
        endpoint
        $9
        127.0.0.1
        $4
        role
        $7
        replica
        $18
        replication-offset
        :72100
        $6
        health
        $6
        online

        """.trimIndent().replace("\n", "\r\n"),
    )

    // RESP3 variant: shard and nodes as maps; empty hostname must map to null
    fun resp3ClusterShardsResponse(): Buffer = respBuffer(
        """
        *1
        %2
        $5
        slots
        *2
        :0
        :16383
        $5
        nodes
        *1
        %9
        $2
        id
        $12
        shard-node-1
        $4
        port
        :6379
        $8
        tls-port
        :7443
        $2
        ip
        $9
        127.0.0.1
        $8
        endpoint
        $9
        127.0.0.1
        $8
        hostname
        $0

        $4
        role
        $7
        replica
        $18
        replication-offset
        :72100
        $6
        health
        $7
        loading

        """.trimIndent().replace("\n", "\r\n"),
    )

    fun validSentinelMasterAddressResponse(): Buffer = respBuffer(
        """
        *2
        +127.0.0.1
        +6379
        
        """.trimIndent().replace("\n", "\r\n"),
    )

    fun malformedSentinelMasterAddressResponse(): Buffer = respBuffer(
        """
        *a
        
        """.trimIndent().replace("\n", "\r\n"),
    )

    fun validSentinelReplicasResponse(): Buffer = respBuffer(
        """
        *2
        +id-1 127.0.0.1:6380@16380 slave
        +id-2 127.0.0.1:6381@16381 slave
        
        """.trimIndent().replace("\n", "\r\n"),
    )

    fun malformedSentinelReplicasResponse(): Buffer = respBuffer(
        """
        *2
        +id-1 127.0.0.1:6380@16380 slave
        #x
        
        """.trimIndent().replace("\n", "\r\n"),
    )
}
