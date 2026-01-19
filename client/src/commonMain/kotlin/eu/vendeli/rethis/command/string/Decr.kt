package eu.vendeli.rethis.command.string

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.string.DecrCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.decr(key: String): Long {
    val request = if (cfg.withSlots) {
        DecrCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        DecrCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return DecrCommandCodec.decode(topology.handle(request), cfg.charset)
}
