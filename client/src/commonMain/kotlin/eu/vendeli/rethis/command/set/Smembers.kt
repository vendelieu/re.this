package eu.vendeli.rethis.command.`set`

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.`set`.SMembersCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String
import kotlin.collections.Set

public suspend fun ReThis.sMembers(key: String): Set<String> {
    val request = if(cfg.withSlots) {
        SMembersCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        SMembersCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return SMembersCommandCodec.decode(topology.handle(request), cfg.charset)
}
