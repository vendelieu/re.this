package eu.vendeli.rethis.command.hash

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.hash.HPersistCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.hPersist(key: String, vararg `field`: String): List<Long> {
    val request = if(cfg.withSlots) {
        HPersistCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, field = field)
    } else {
        HPersistCommandCodec.encode(charset = cfg.charset, key = key, field = field)
    }
    return HPersistCommandCodec.decode(topology.handle(request), cfg.charset)
}
