package eu.vendeli.rethis.command.list

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.list.LTrimCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.lTrim(
    key: String,
    start: Long,
    stop: Long,
): String {
    val request = if (cfg.withSlots) {
        LTrimCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, start = start, stop = stop)
    } else {
        LTrimCommandCodec.encode(charset = cfg.charset, key = key, start = start, stop = stop)
    }
    return LTrimCommandCodec.decode(topology.handle(request), cfg.charset)
}
