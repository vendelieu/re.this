package eu.vendeli.rethis.command.hash

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.hash.HIncrByFloatCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.hIncrByFloat(
    key: String,
    `field`: String,
    increment: Double,
): Double {
    val request = if(cfg.withSlots) {
        HIncrByFloatCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, field = field, increment = increment)
    } else {
        HIncrByFloatCommandCodec.encode(charset = cfg.charset, key = key, field = field, increment = increment)
    }
    return HIncrByFloatCommandCodec.decode(topology.handle(request), cfg.charset)
}
