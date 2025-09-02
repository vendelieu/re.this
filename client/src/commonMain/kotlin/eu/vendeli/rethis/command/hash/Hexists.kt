package eu.vendeli.rethis.command.hash

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.hash.HExistsCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.hExists(key: String, `field`: String): Boolean {
    val request = if(cfg.withSlots) {
        HExistsCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, field = field)
    } else {
        HExistsCommandCodec.encode(charset = cfg.charset, key = key, field = field)
    }
    return HExistsCommandCodec.decode(topology.handle(request), cfg.charset)
}
