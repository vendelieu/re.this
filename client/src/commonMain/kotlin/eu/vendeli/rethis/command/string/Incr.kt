package eu.vendeli.rethis.command.string

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.string.IncrCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.incr(key: String): Long {
    val request = if (cfg.withSlots) {
        IncrCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        IncrCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return IncrCommandCodec.decode(topology.handle(request), cfg.charset)
}
