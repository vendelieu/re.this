package eu.vendeli.rethis.command.`set`

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.`set`.SMisMemberCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Boolean
import kotlin.String
import kotlin.collections.List

public suspend fun ReThis.sMisMember(key: String, vararg member: String): List<Boolean> {
    val request = if(cfg.withSlots) {
        SMisMemberCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, member = member)
    } else {
        SMisMemberCommandCodec.encode(charset = cfg.charset, key = key, member = member)
    }
    return SMisMemberCommandCodec.decode(topology.handle(request), cfg.charset)
}
