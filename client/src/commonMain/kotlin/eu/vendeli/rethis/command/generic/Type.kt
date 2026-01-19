package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.generic.TypeCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.type(key: String): String {
    val request = if (cfg.withSlots) {
        TypeCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        TypeCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return TypeCommandCodec.decode(topology.handle(request), cfg.charset)
}
