package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.ClusterMeetCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.Long
import kotlin.String

public suspend fun ReThis.clusterMeet(
    ip: String,
    port: Long,
    clusterBusPort: Long? = null,
): Boolean {
    val request = if(cfg.withSlots) {
        ClusterMeetCommandCodec.encodeWithSlot(charset = cfg.charset, ip = ip, port = port, clusterBusPort = clusterBusPort)
    } else {
        ClusterMeetCommandCodec.encode(charset = cfg.charset, ip = ip, port = port, clusterBusPort = clusterBusPort)
    }
    return ClusterMeetCommandCodec.decode(topology.handle(request), cfg.charset)
}
