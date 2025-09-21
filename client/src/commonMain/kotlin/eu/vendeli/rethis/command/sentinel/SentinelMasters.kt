package eu.vendeli.rethis.command.sentinel

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sentinel.SentinelMastersCommandCodec
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.sentinelMasters(): List<RType> {
    val request = if(cfg.withSlots) {
        SentinelMastersCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        SentinelMastersCommandCodec.encode(charset = cfg.charset, )
    }
    return SentinelMastersCommandCodec.decode(topology.handle(request), cfg.charset)
}
