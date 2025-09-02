package eu.vendeli.rethis.command.string

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.string.IncrByCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.incrBy(key: String, increment: Long): Long {
    val request = if(cfg.withSlots) {
        IncrByCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, increment = increment)
    } else {
        IncrByCommandCodec.encode(charset = cfg.charset, key = key, increment = increment)
    }
    return IncrByCommandCodec.decode(topology.handle(request), cfg.charset)
}
