package eu.vendeli.rethis.command.sortedset

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.sortedset.ZRemCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.zRem(key: String, vararg member: String): Long {
    val request = if(cfg.withSlots) {
        ZRemCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, member = member)
    } else {
        ZRemCommandCodec.encode(charset = cfg.charset, key = key, member = member)
    }
    return ZRemCommandCodec.decode(topology.handle(request), cfg.charset)
}
