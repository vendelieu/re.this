package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.MemoryMallocStatsCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.memoryMallocStats(): String {
    val request = if(cfg.withSlots) {
        MemoryMallocStatsCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        MemoryMallocStatsCommandCodec.encode(charset = cfg.charset, )
    }
    return MemoryMallocStatsCommandCodec.decode(topology.handle(request), cfg.charset)
}
