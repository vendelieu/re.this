package eu.vendeli.rethis.command.hash

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.hash.HStrlenCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.hStrlen(key: String, `field`: String): Long {
    val request = if (cfg.withSlots) {
        HStrlenCommandCodec.encodeWithSlot(charset = cfg.charset, key = key, field = field)
    } else {
        HStrlenCommandCodec.encode(charset = cfg.charset, key = key, field = field)
    }
    return HStrlenCommandCodec.decode(topology.handle(request), cfg.charset)
}
