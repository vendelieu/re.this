package eu.vendeli.rethis.command.list

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.list.LRemCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.lRem(
    key: String,
    count: Long,
    element: String,
): Long {
    val request = if(cfg.withSlots) {
        LRemCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, count = count, element = element)
    } else {
        LRemCommandCodec.encode(charset = cfg.charset, key = key, count = count, element = element)
    }
    return LRemCommandCodec.decode(topology.handle(request), cfg.charset)
}
