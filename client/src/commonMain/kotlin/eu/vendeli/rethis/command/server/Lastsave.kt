package eu.vendeli.rethis.command.server

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.server.LastSaveCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.lastSave(): Long {
    val request = if (cfg.withSlots) {
        LastSaveCommandCodec.encodeWithSlot(charset = cfg.charset)
    } else {
        LastSaveCommandCodec.encode(charset = cfg.charset)
    }
    return LastSaveCommandCodec.decode(topology.handle(request), cfg.charset)
}
