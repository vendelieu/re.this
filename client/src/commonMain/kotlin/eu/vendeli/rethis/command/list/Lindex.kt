package eu.vendeli.rethis.command.list

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.list.LIndexCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.lIndex(key: String, index: Long): String? {
    val request = if(cfg.withSlots) {
        LIndexCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, index = index)
    } else {
        LIndexCommandCodec.encode(charset = cfg.charset, key = key, index = index)
    }
    return LIndexCommandCodec.decode(topology.handle(request), cfg.charset)
}
