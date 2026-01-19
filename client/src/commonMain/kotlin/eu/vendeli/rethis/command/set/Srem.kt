package eu.vendeli.rethis.command.`set`

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.set.SRemCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.sRem(key: String, vararg member: String): Long {
    val request = if (cfg.withSlots) {
        SRemCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, member = member)
    } else {
        SRemCommandCodec.encode(charset = cfg.charset, key = key, member = member)
    }
    return SRemCommandCodec.decode(topology.handle(request), cfg.charset)
}
