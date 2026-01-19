package eu.vendeli.rethis.command.list

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.list.LRangeCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.lRange(
    key: String,
    start: Long,
    stop: Long,
): List<String> {
    val request = if (cfg.withSlots) {
        LRangeCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, start = start, stop = stop)
    } else {
        LRangeCommandCodec.encode(charset = cfg.charset, key = key, start = start, stop = stop)
    }
    return LRangeCommandCodec.decode(topology.handle(request), cfg.charset)
}
