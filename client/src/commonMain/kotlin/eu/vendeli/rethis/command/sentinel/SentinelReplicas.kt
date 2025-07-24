package eu.vendeli.rethis.command.sentinel

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.codecs.sentinel.SentinelReplicasCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.sentinelReplicas(masterName: String): List<RType> {
    val request = if(cfg.withSlots) {
        SentinelReplicasCommandCodec.encodeWithSlot(charset = cfg.charset, masterName = masterName)
    } else {
        SentinelReplicasCommandCodec.encode(charset = cfg.charset, masterName = masterName)
    }
    return SentinelReplicasCommandCodec.decode(topology.handle(request), cfg.charset)
}
