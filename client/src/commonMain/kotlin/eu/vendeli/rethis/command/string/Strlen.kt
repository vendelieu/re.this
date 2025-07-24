package eu.vendeli.rethis.command.string

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.string.StrlenCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.strlen(key: String): Long {
    val request = if(cfg.withSlots) {
        StrlenCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        StrlenCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return StrlenCommandCodec.decode(topology.handle(request), cfg.charset)
}
