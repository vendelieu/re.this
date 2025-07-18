package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.api.spec.common.types.RType
import eu.vendeli.rethis.codecs.server.MemoryStatsCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String
import kotlin.collections.Map

public suspend fun ReThis.memoryStats(): Map<String, RType> {
    val request = if(cfg.withSlots) {
        MemoryStatsCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        MemoryStatsCommandCodec.encode(charset = cfg.charset, )
    }
    return MemoryStatsCommandCodec.decode(topology.handle(request), cfg.charset)
}
