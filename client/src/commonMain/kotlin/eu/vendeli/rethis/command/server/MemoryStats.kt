package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.MemoryStatsCommandCodec
import eu.vendeli.rethis.shared.types.RType
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.memoryStats(): Map<String, RType> {
    val request = if(cfg.withSlots) {
        MemoryStatsCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        MemoryStatsCommandCodec.encode(charset = cfg.charset, )
    }
    return MemoryStatsCommandCodec.decode(topology.handle(request), cfg.charset)
}
