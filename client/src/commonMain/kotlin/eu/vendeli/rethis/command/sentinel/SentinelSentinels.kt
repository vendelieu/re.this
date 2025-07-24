package eu.vendeli.rethis.command.sentinel

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.codecs.sentinel.SentinelSentinelsCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.sentinelSentinels(masterName: String): List<RType> {
    val request = if(cfg.withSlots) {
        SentinelSentinelsCommandCodec.encodeWithSlot(charset = cfg.charset, masterName = masterName)
    } else {
        SentinelSentinelsCommandCodec.encode(charset = cfg.charset, masterName = masterName)
    }
    return SentinelSentinelsCommandCodec.decode(topology.handle(request), cfg.charset)
}
