package eu.vendeli.rethis.command.hash

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.hash.HDelCommandCodec
import eu.vendeli.rethis.topology.handle
import kotlin.Long
import kotlin.String

public suspend fun ReThis.hDel(key: String, vararg `field`: String): Long {
    val request = if(cfg.withSlots) {
        HDelCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, field = field)
    } else {
        HDelCommandCodec.encode(charset = cfg.charset, key = key, field = field)
    }
    return HDelCommandCodec.decode(topology.handle(request), cfg.charset)
}
