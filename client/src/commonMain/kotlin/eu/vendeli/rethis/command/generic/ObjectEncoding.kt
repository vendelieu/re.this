package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.generic.ObjectEncodingCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.objectEncoding(key: String): String? {
    val request = if(cfg.withSlots) {
        ObjectEncodingCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        ObjectEncodingCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return ObjectEncodingCommandCodec.decode(topology.handle(request), cfg.charset)
}
