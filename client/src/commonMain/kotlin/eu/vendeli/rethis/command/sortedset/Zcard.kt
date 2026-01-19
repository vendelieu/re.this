package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sortedset.ZCardCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.zCard(key: String): Long {
    val request = if (cfg.withSlots) {
        ZCardCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        ZCardCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return ZCardCommandCodec.decode(topology.handle(request), cfg.charset)
}
