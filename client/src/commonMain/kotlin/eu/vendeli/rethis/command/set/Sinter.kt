package eu.vendeli.rethis.command.`set`

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.`set`.SInterCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.String
import kotlin.collections.Set

public suspend fun ReThis.sInter(vararg key: String): Set<String> {
    val request = if(cfg.withSlots) {
        SInterCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        SInterCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return SInterCommandCodec.decode(topology.handle(request), cfg.charset)
}
