package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.generic.TtlCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.ttl(key: String): Long {
    val request = if (cfg.withSlots) {
        TtlCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        TtlCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return TtlCommandCodec.decode(topology.handle(request), cfg.charset)
}
