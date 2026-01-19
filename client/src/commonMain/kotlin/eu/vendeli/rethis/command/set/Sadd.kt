package eu.vendeli.rethis.command.`set`

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.set.SAddCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.sAdd(key: String, vararg member: String): Long {
    val request = if (cfg.withSlots) {
        SAddCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, member = member)
    } else {
        SAddCommandCodec.encode(charset = cfg.charset, key = key, member = member)
    }
    return SAddCommandCodec.decode(topology.handle(request), cfg.charset)
}
