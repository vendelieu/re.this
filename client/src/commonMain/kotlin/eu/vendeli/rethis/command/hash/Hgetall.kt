package eu.vendeli.rethis.command.hash

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.hash.HGetAllCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.hGetAll(key: String): Map<String, String?> {
    val request = if(cfg.withSlots) {
        HGetAllCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        HGetAllCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return HGetAllCommandCodec.decode(topology.handle(request), cfg.charset)
}
