package eu.vendeli.rethis.command.`set`

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.set.SIsMemberCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.sIsMember(key: String, member: String): Boolean {
    val request = if (cfg.withSlots) {
        SIsMemberCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, member = member)
    } else {
        SIsMemberCommandCodec.encode(charset = cfg.charset, key = key, member = member)
    }
    return SIsMemberCommandCodec.decode(topology.handle(request), cfg.charset)
}
