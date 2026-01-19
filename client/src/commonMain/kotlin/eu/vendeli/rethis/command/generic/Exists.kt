package eu.vendeli.rethis.command.generic

import eu.vendeli.rethis.ReThis
import eu.vendeli.rethis.codecs.generic.ExistsCommandCodec
import eu.vendeli.rethis.topology.handle

public suspend fun ReThis.exists(vararg key: String): Long {
    val request = if (cfg.withSlots) {
        ExistsCommandCodec.encodeWithSlot(charset = cfg.charset, key = key)
    } else {
        ExistsCommandCodec.encode(charset = cfg.charset, key = key)
    }
    return ExistsCommandCodec.decode(topology.handle(request), cfg.charset)
}
