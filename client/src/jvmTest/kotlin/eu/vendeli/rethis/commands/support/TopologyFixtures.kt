package eu.vendeli.rethis.commands.support

import eu.vendeli.rethis.shared.utils.EMPTY_BUFFER
import io.ktor.utils.io.core.writeFully
import kotlinx.io.Buffer

internal object TopologyFixtures {
    fun respBuffer(payload: String): Buffer = Buffer().apply {
        writeFully(payload.encodeToByteArray())
    }

    fun emptyBuffer(): Buffer = EMPTY_BUFFER

    fun validClusterSlotsResponse(): Buffer = respBuffer(
        """
        *1
        *4
        0
        16383
        127.0.0.1
        7000
        master-id
        *0
        127.0.0.1
        7001
        replica-id
        *0
        
        """.trimIndent().replace("\n", "\r\n"),
    )

    fun malformedClusterSlotsResponse(): Buffer = respBuffer(
        """
        *1
        +4
        
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
