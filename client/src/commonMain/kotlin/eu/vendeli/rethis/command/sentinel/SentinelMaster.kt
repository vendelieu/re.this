package eu.vendeli.rethis.command.sentinel

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.codecs.sentinel.SentinelMasterCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.sentinelMaster(masterName: String): List<RType> {
    val request = if(cfg.withSlots) {
        SentinelMasterCommandCodec.encodeWithSlot(charset = cfg.charset, masterName = masterName)
    } else {
        SentinelMasterCommandCodec.encode(charset = cfg.charset, masterName = masterName)
    }
    return SentinelMasterCommandCodec.decode(topology.handle(request), cfg.charset)
}
