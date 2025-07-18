package eu.vendeli.rethis.command.cluster

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.cluster.ClusterSetConfigEpochCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.Long

public suspend fun ReThis.clusterSetConfigEpoch(configEpoch: Long): Boolean {
    val request = if(cfg.withSlots) {
        ClusterSetConfigEpochCommandCodec.encodeWithSlot(charset = cfg.charset, configEpoch = configEpoch)
    } else {
        ClusterSetConfigEpochCommandCodec.encode(charset = cfg.charset, configEpoch = configEpoch)
    }
    return ClusterSetConfigEpochCommandCodec.decode(topology.handle(request), cfg.charset)
}
