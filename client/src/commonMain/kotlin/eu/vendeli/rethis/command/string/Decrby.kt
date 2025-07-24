package eu.vendeli.rethis.command.string

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.string.DecrByCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.decrBy(key: String, decrement: Long): Long {
    val request = if(cfg.withSlots) {
        DecrByCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, decrement = decrement)
    } else {
        DecrByCommandCodec.encode(charset = cfg.charset, key = key, decrement = decrement)
    }
    return DecrByCommandCodec.decode(topology.handle(request), cfg.charset)
}
