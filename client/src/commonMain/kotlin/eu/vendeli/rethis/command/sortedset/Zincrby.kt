package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sortedset.ZIncrByCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.zIncrBy(
    key: String,
    member: String,
    increment: Long,
): Double {
    val request = if(cfg.withSlots) {
        ZIncrByCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, member = member, increment = increment)
    } else {
        ZIncrByCommandCodec.encode(charset = cfg.charset, key = key, member = member, increment = increment)
    }
    return ZIncrByCommandCodec.decode(topology.handle(request), cfg.charset)
}
