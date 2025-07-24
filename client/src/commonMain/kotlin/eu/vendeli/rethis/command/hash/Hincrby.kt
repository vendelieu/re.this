package eu.vendeli.rethis.command.hash

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.hash.HIncrByCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.hIncrBy(
    key: String,
    `field`: String,
    increment: Long,
): Long {
    val request = if(cfg.withSlots) {
        HIncrByCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, field = field, increment = increment)
    } else {
        HIncrByCommandCodec.encode(charset = cfg.charset, key = key, field = field, increment = increment)
    }
    return HIncrByCommandCodec.decode(topology.handle(request), cfg.charset)
}
