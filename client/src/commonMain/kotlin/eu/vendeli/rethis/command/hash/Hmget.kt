package eu.vendeli.rethis.command.hash

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.hash.HMGetCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.hMGet(key: String, vararg `field`: String): List<String?> {
    val request = if(cfg.withSlots) {
        HMGetCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, field = field)
    } else {
        HMGetCommandCodec.encode(charset = cfg.charset, key = key, field = field)
    }
    return HMGetCommandCodec.decode(topology.handle(request), cfg.charset)
}
