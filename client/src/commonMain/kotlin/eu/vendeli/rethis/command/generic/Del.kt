package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.generic.DelCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.del(vararg key: String): Long {
    val request = if(cfg.withSlots) {
        DelCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        DelCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return DelCommandCodec.decode(topology.handle(request), cfg.charset)
}
