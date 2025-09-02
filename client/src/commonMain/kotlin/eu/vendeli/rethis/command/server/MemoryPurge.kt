package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.MemoryPurgeCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.memoryPurge(): Boolean {
    val request = if(cfg.withSlots) {
        MemoryPurgeCommandCodec.encodeWithSlot(charset = cfg.charset, )
    } else {
        MemoryPurgeCommandCodec.encode(charset = cfg.charset, )
    }
    return MemoryPurgeCommandCodec.decode(topology.handle(request), cfg.charset)
}
