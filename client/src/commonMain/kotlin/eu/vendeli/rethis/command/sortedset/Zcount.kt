package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sortedset.ZCountCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.zCount(
    key: String,
    min: Double,
    max: Double,
): Long {
    val request = if (cfg.withSlots) {
        ZCountCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, min = min, max = max)
    } else {
        ZCountCommandCodec.encode(charset = cfg.charset, key = key, min = min, max = max)
    }
    return ZCountCommandCodec.decode(topology.handle(request), cfg.charset)
}
